package com.github.bymaskeli53.kotlindesignpatternscreator.generators

import com.github.bymaskeli53.kotlindesignpatternscreator.PluginBundle
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.PsiHelper
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.companionOrNull
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.functions
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.getOrCreateBody
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory

object FactoryGenerator {

    fun apply(project: Project, ktClass: KtClass) {
        val name = ktClass.name ?: return

        if (!ktClass.isInterface() && !ktClass.hasModifier(KtTokens.ABSTRACT_KEYWORD)) {
            PsiHelper.warn(project, PluginBundle["warn.notAbstract"])
            return
        }
        if (ktClass.companionOrNull()?.declarations?.any { it.name == "create" } == true) {
            PsiHelper.warn(project, PluginBundle["warn.alreadyApplied", "Factory"])
            return
        }

        val factory = KtPsiFactory(project)
        val superCall = if (ktClass.isInterface()) name else "$name()"

        val overridesText = ktClass.functions()
            .filter { it.needsOverride(isInterface = ktClass.isInterface()) }
            .joinToString("\n") { "    ${it.overrideStub()}" }
            .ifBlank { "    // TODO: implement" }

        val enumText = "enum class ${name}Type { IMPL_A, IMPL_B }"
        val implAText = "class ${name}ImplA : $superCall {\n$overridesText\n}"
        val implBText = "class ${name}ImplB : $superCall {\n$overridesText\n}"
        val companionText = """
            companion object {
                fun create(type: ${name}Type): $name = when (type) {
                    ${name}Type.IMPL_A -> ${name}ImplA()
                    ${name}Type.IMPL_B -> ${name}ImplB()
                }
            }
        """.trimIndent()

        val enumClass = factory.createClass(enumText)
        val implA = factory.createClass(implAText)
        val implB = factory.createClass(implBText)

        val body = ktClass.getOrCreateBody(factory)
        val rBrace = body.rBrace

        fun insert(element: org.jetbrains.kotlin.psi.KtDeclaration) {
            if (rBrace != null) body.addBefore(element, rBrace) else body.add(element)
        }

        insert(enumClass)
        insert(implA)
        insert(implB)

        val existingCompanion = ktClass.companionOrNull()
        if (existingCompanion != null) {
            val createFn = factory.createFunction(
                """
                fun create(type: ${name}Type): $name = when (type) {
                    ${name}Type.IMPL_A -> ${name}ImplA()
                    ${name}Type.IMPL_B -> ${name}ImplB()
                }
                """.trimIndent()
            )
            val companionBody = existingCompanion.body
            if (companionBody?.rBrace != null) {
                companionBody.addBefore(createFn, companionBody.rBrace)
            } else {
                existingCompanion.add(createFn)
            }
        } else {
            val companionObj = factory.createCompanionObject(companionText)
            insert(companionObj)
        }

        PsiHelper.reformat(ktClass)
        PsiHelper.notify(project, PluginBundle["notification.success", "Factory"], NotificationType.INFORMATION)
    }

    private fun KtNamedFunction.needsOverride(isInterface: Boolean): Boolean {
        // A function needs an override stub when it has no body — for an interface
        // that's any bodyless declaration, for an abstract class only those marked
        // `abstract` (which by Kotlin rules are also bodyless, but we double-check).
        if (bodyExpression != null) return false
        return isInterface || hasModifier(KtTokens.ABSTRACT_KEYWORD)
    }

    private fun KtNamedFunction.overrideStub(): String {
        val fnName = name ?: return ""
        val carriedModifiers = listOfNotNull(
            "suspend".takeIf { hasModifier(KtTokens.SUSPEND_KEYWORD) },
            "operator".takeIf { hasModifier(KtTokens.OPERATOR_KEYWORD) },
            "infix".takeIf { hasModifier(KtTokens.INFIX_KEYWORD) },
            "inline".takeIf { hasModifier(KtTokens.INLINE_KEYWORD) }
        ).joinToString(" ").let { if (it.isNotBlank()) "$it " else "" }

        val typeParams = typeParameterList?.text?.let { "$it " } ?: ""
        val receiver = receiverTypeReference?.text?.let { "$it." } ?: ""
        val params = valueParameters.joinToString(", ") { p ->
            val t = p.typeReference?.text ?: "Any"
            "${p.name ?: "_"}: $t"
        }
        val ret = typeReference?.text
        val retSuffix = if (ret != null && ret != "Unit") ": $ret" else ""
        return "override ${carriedModifiers}fun $typeParams$receiver$fnName($params)$retSuffix = TODO()"
    }
}
