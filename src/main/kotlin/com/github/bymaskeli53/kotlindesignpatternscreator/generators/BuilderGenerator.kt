package com.github.bymaskeli53.kotlindesignpatternscreator.generators

import com.github.bymaskeli53.kotlindesignpatternscreator.PluginBundle
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.PsiHelper
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.getOrCreateBody
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.hasNestedClassNamed
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPsiFactory

object BuilderGenerator {

    fun apply(
        project: Project,
        ktClass: KtClass,
        required: List<KtParameter>,
        optional: List<KtParameter>
    ) {
        val className = ktClass.name ?: return

        if (ktClass.hasNestedClassNamed("Builder")) {
            PsiHelper.warn(project, PluginBundle["warn.alreadyApplied", "Builder"])
            return
        }

        val factory = KtPsiFactory(project)
        val optionalSet = optional.toSet()

        // 1. Rebuild primary constructor: private + optional params become nullable.
        val existingPrimary = ktClass.primaryConstructor
        if (existingPrimary != null) {
            val paramsText = ktClass.primaryConstructorParameters.joinToString(", ") { p ->
                val valVar = when {
                    p.isMutable -> "var "
                    p.hasValOrVar() -> "val "
                    else -> ""
                }
                val origType = p.typeReference?.text ?: "Any"
                val type = if (p in optionalSet && !origType.endsWith("?")) "$origType?" else origType
                "$valVar${p.name}: $type"
            }
            val newCtor = factory.createPrimaryConstructor("private constructor($paramsText)")
            existingPrimary.replace(newCtor)
        }

        // 2. Build the public Builder class — callers invoke it as `Person.Builder(...)`.
        val builderText = buildString {
            val builderCtorParams = required.joinToString(", ") { p ->
                "private val ${p.name}: ${p.typeReference?.text ?: "Any"}"
            }
            if (builderCtorParams.isNotEmpty()) {
                appendLine("class Builder($builderCtorParams) {")
            } else {
                appendLine("class Builder {")
            }

            optional.forEach { p ->
                val origType = p.typeReference?.text ?: "Any"
                val nullableType = if (origType.endsWith("?")) origType else "$origType?"
                appendLine("    private var ${p.name}: $nullableType = null")
            }
            if (optional.isNotEmpty()) appendLine()

            optional.forEach { p ->
                val setterType = p.typeReference?.text ?: "Any"
                appendLine("    fun ${p.name}(${p.name}: $setterType) = apply { this.${p.name} = ${p.name} }")
            }
            if (optional.isNotEmpty()) appendLine()

            appendLine("    fun build(): $className {")
            appendLine("        return $className(")
            val allProps = ktClass.primaryConstructorParameters
            allProps.forEachIndexed { i, p ->
                val sep = if (i < allProps.size - 1) "," else ""
                appendLine("            ${p.name} = ${p.name}$sep")
            }
            appendLine("        )")
            appendLine("    }")
            append("}")
        }
        val builderClass = factory.createClass(builderText)

        val body = ktClass.getOrCreateBody(factory)
        val rBrace = body.rBrace
        if (rBrace != null) body.addBefore(builderClass, rBrace) else body.add(builderClass)

        PsiHelper.reformat(ktClass)
        PsiHelper.notify(project, PluginBundle["notification.success", "Builder"], NotificationType.INFORMATION)
    }
}
