package com.github.bymaskeli53.kotlindesignpatternscreator.actions

import com.github.bymaskeli53.kotlindesignpatternscreator.generators.ObserverGenerator
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.psi.KtClass

class ObserverAction : BasePatternAction() {
    override fun apply(project: Project, ktClass: KtClass) {
        ObserverGenerator.apply(project, ktClass)
    }
}
