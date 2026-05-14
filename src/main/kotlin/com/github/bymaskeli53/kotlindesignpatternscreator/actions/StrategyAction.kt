package com.github.bymaskeli53.kotlindesignpatternscreator.actions

import com.github.bymaskeli53.kotlindesignpatternscreator.PluginBundle
import com.github.bymaskeli53.kotlindesignpatternscreator.dialog.MethodSelectorDialog
import com.github.bymaskeli53.kotlindesignpatternscreator.generators.StrategyGenerator
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.PsiHelper
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.functions
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtClass

class StrategyAction : BasePatternAction() {

    override fun runPattern(project: Project, ktClass: KtClass, editor: Editor?) {
        val methods = ktClass.functions()
        if (methods.isEmpty()) {
            PsiHelper.warn(project, PluginBundle["warn.noMethods"])
            return
        }
        val dialog = MethodSelectorDialog(project, methods)
        if (!dialog.showAndGet()) return
        val selected = dialog.selectedFunction() ?: return
        runInWriteCommand(project) {
            StrategyGenerator.apply(project, ktClass, selected)
        }
    }
}
