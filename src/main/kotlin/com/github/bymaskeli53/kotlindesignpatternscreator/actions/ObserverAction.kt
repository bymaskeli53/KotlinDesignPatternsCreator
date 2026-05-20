package com.github.bymaskeli53.kotlindesignpatternscreator.actions

import com.github.bymaskeli53.kotlindesignpatternscreator.dialog.MethodSelectorDialog
import com.github.bymaskeli53.kotlindesignpatternscreator.generators.ObserverGenerator
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.functions
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtNamedFunction

class ObserverAction : BasePatternAction() {

    override fun runPattern(project: Project, ktClass: KtClass, editor: Editor?) {
        val methodsToNotify: List<KtNamedFunction> = ktClass.functions().let { fns ->
            if (fns.isEmpty()) {
                emptyList()
            } else {
                val dialog = MethodSelectorDialog(
                    project = project,
                    functions = fns,
                    multiSelect = true,
                    titleKey = "dialog.observer.title",
                    labelKey = "dialog.observer.label",
                    allowEmptySelection = true
                )
                if (!dialog.showAndGet()) return
                dialog.selectedFunctions()
            }
        }
        runInWriteCommand(project) {
            ObserverGenerator.apply(project, ktClass, methodsToNotify)
        }
    }
}
