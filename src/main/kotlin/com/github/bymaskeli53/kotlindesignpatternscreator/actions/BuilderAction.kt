package com.github.bymaskeli53.kotlindesignpatternscreator.actions

import com.github.bymaskeli53.kotlindesignpatternscreator.PluginBundle
import com.github.bymaskeli53.kotlindesignpatternscreator.dialog.PropertyRequirednessDialog
import com.github.bymaskeli53.kotlindesignpatternscreator.generators.BuilderGenerator
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.PsiHelper
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.primaryConstructorProperties
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtClass

class BuilderAction : BasePatternAction() {

    override fun runPattern(project: Project, ktClass: KtClass, editor: Editor?) {
        val props = ktClass.primaryConstructorProperties()
        if (props.isEmpty()) {
            PsiHelper.warn(project, PluginBundle["warn.noProperties"])
            return
        }
        val dialog = PropertyRequirednessDialog(project, props)
        if (!dialog.showAndGet()) return
        val (required, optional) = props.partition { dialog.isRequired(it) }
        runInWriteCommand(project) {
            BuilderGenerator.apply(project, ktClass, required, optional)
        }
    }
}
