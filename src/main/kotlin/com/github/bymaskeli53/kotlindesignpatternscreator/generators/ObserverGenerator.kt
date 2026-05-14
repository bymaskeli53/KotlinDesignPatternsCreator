package com.github.bymaskeli53.kotlindesignpatternscreator.generators

import com.github.bymaskeli53.kotlindesignpatternscreator.PluginBundle
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.PsiHelper
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.getOrCreateBody
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtPsiFactory

object ObserverGenerator {

    fun apply(project: Project, ktClass: KtClass) {
        val className = ktClass.name ?: return
        val observerName = "${className}Observer"

        val alreadyApplied = ktClass.declarations.any { it.name == observerName } ||
            ktClass.declarations.any { it.name == "observers" }
        if (alreadyApplied) {
            PsiHelper.warn(project, PluginBundle["warn.alreadyApplied", "Observer"])
            return
        }

        val factory = KtPsiFactory(project)
        val observerInterface = factory.createClass(
            "fun interface $observerName {\n    fun onChanged(data: Any)\n}"
        )
        val observersField = factory.createProperty(
            "private val observers = mutableListOf<$observerName>()"
        )
        val addFn = factory.createFunction(
            "fun addObserver(observer: $observerName) {\n    observers.add(observer)\n}"
        )
        val removeFn = factory.createFunction(
            "fun removeObserver(observer: $observerName) {\n    observers.remove(observer)\n}"
        )
        val notifyFn = factory.createFunction(
            "private fun notifyObservers(data: Any) {\n    observers.forEach { it.onChanged(data) }\n}"
        )

        val body = ktClass.getOrCreateBody(factory)
        val rBrace = body.rBrace

        fun insert(element: KtDeclaration) {
            if (rBrace != null) body.addBefore(element, rBrace) else body.add(element)
        }

        insert(observerInterface)
        insert(observersField)
        insert(addFn)
        insert(removeFn)
        insert(notifyFn)

        PsiHelper.reformat(ktClass)
        PsiHelper.notify(project, PluginBundle["notification.success", "Observer"], NotificationType.INFORMATION)
    }
}
