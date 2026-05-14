package com.github.bymaskeli53.kotlindesignpatternscreator.generators

import com.github.bymaskeli53.kotlindesignpatternscreator.PluginBundle
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.PsiHelper
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.companionOrNull
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.getOrCreateBody
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.hasNestedClassNamed
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.primaryConstructorProperties
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtPsiFactory

object BuilderGenerator {

    fun apply(project: Project, ktClass: KtClass) {
        val className = ktClass.name ?: return

        if (ktClass.hasNestedClassNamed("Builder") || ktClass.companionOrNull()?.builderFunctionExists() == true) {
            PsiHelper.warn(project, PluginBundle["warn.alreadyApplied", "Builder"])
            return
        }

        val props = ktClass.primaryConstructorProperties()
        if (props.isEmpty()) {
            PsiHelper.warn(project, PluginBundle["warn.noProperties"])
            return
        }

        val factory = KtPsiFactory(project)

        // 1. Make primary constructor private by replacing it with an explicit
        //    `private constructor(...)` form, preserving original parameter text.
        val existingPrimary = ktClass.primaryConstructor
        if (existingPrimary != null) {
            val paramsText = ktClass.primaryConstructorParameters.joinToString(", ") { it.text }
            val newCtor = factory.createPrimaryConstructor("private constructor($paramsText)")
            existingPrimary.replace(newCtor)
        }

        // 2. Build the inner Builder class.
        val builderText = buildString {
            appendLine("class Builder {")
            props.forEach { p ->
                val type = p.typeReference?.text ?: "Any"
                appendLine("    private var ${p.name}: $type? = null")
            }
            appendLine()
            props.forEach { p ->
                val type = p.typeReference?.text ?: "Any"
                appendLine("    fun ${p.name}(${p.name}: $type) = apply { this.${p.name} = ${p.name} }")
            }
            appendLine()
            appendLine("    fun build(): $className {")
            appendLine("        return $className(")
            props.forEachIndexed { i, p ->
                val sep = if (i < props.size - 1) "," else ""
                appendLine("            ${p.name} = requireNotNull(${p.name}) { \"${p.name} is required\" }$sep")
            }
            appendLine("        )")
            appendLine("    }")
            append("}")
        }
        val builderClass = factory.createClass(builderText)

        val body = ktClass.getOrCreateBody(factory)
        val rBrace = body.rBrace
        if (rBrace != null) body.addBefore(builderClass, rBrace) else body.add(builderClass)

        // 3. Add or update companion object with builder() factory.
        val existingCompanion = ktClass.companionOrNull()
        if (existingCompanion != null) {
            val builderFn = factory.createFunction("fun builder() = Builder()")
            val companionBody = existingCompanion.body
            if (companionBody?.rBrace != null) {
                companionBody.addBefore(builderFn, companionBody.rBrace)
            } else {
                existingCompanion.add(builderFn)
            }
        } else {
            val companionObj = factory.createCompanionObject(
                "companion object {\n    fun builder() = Builder()\n}"
            )
            if (rBrace != null) body.addBefore(companionObj, rBrace) else body.add(companionObj)
        }

        PsiHelper.reformat(ktClass)
        PsiHelper.notify(project, PluginBundle["notification.success", "Builder"], NotificationType.INFORMATION)
    }

    private fun KtObjectDeclaration.builderFunctionExists(): Boolean =
        declarations.any { it.name == "builder" }
}
