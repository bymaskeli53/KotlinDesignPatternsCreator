package com.github.bymaskeli53.kotlindesignpatternscreator.generators

import com.github.bymaskeli53.kotlindesignpatternscreator.PluginBundle
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.PsiHelper
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.getOrCreateBody
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory

object StrategyGenerator {

    fun apply(project: Project, ktClass: KtClass, fn: KtNamedFunction) {
        val fnName = fn.name ?: return
        val strategyName = fnName.replaceFirstChar { it.uppercaseChar() } + "Strategy"
        val fieldName = "${fnName}Strategy"

        if (ktClass.declarations.any { it.name == strategyName } ||
            ktClass.declarations.any { it.name == fieldName }
        ) {
            PsiHelper.warn(project, PluginBundle["warn.alreadyApplied", "Strategy"])
            return
        }

        val factory = KtPsiFactory(project)

        val paramsDecl = fn.valueParameters.joinToString(", ") { p ->
            val type = p.typeReference?.text ?: "Any"
            "${p.name ?: "_"}: $type"
        }
        val paramsCall = fn.valueParameters.joinToString(", ") { it.name ?: "_" }
        val returnType = fn.typeReference?.text
        val returnsUnit = returnType == null || returnType == "Unit"
        val returnSuffix = if (!returnsUnit) ": $returnType" else ""

        val strategyInterface = factory.createClass(
            "fun interface $strategyName {\n    fun $fnName($paramsDecl)$returnSuffix\n}"
        )
        val field = factory.createProperty(
            "private var $fieldName: $strategyName? = null"
        )
        val setter = factory.createFunction(
            "fun set$strategyName(strategy: $strategyName) {\n    this.$fieldName = strategy\n}"
        )

        val body = ktClass.getOrCreateBody(factory)

        val anchor = fn // insert the new declarations right before the original function for locality
        body.addBefore(strategyInterface, anchor)
        body.addBefore(field, anchor)
        body.addBefore(setter, anchor)

        // Replace function body with delegation.
        val newBodyText = if (returnsUnit) {
            "{\n    $fieldName?.$fnName($paramsCall) ?: throw IllegalStateException(\"Strategy not set\")\n}"
        } else {
            "{\n    return $fieldName?.$fnName($paramsCall) ?: throw IllegalStateException(\"Strategy not set\")\n}"
        }
        val newBlock = factory.createBlock(newBodyText.trim().removePrefix("{").removeSuffix("}").trim())

        val existingBody = fn.bodyExpression
        if (existingBody != null) {
            existingBody.replace(newBlock)
        } else {
            // Expression body (= ...) or abstract: append a block body.
            fn.equalsToken?.delete()
            fn.add(newBlock)
        }

        PsiHelper.reformat(ktClass)
        PsiHelper.notify(project, PluginBundle["notification.success", "Strategy"], NotificationType.INFORMATION)
    }
}
