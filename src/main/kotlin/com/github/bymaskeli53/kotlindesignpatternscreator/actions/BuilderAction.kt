package com.github.bymaskeli53.kotlindesignpatternscreator.actions

import com.github.bymaskeli53.kotlindesignpatternscreator.generators.BuilderGenerator
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtClass

class BuilderAction : BasePatternAction() {
    override fun apply(project: Project, ktClass: KtClass) {
        BuilderGenerator.apply(project, ktClass)
    }
}
