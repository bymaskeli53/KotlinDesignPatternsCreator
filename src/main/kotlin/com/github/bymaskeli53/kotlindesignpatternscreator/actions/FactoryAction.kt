package com.github.bymaskeli53.kotlindesignpatternscreator.actions

import com.github.bymaskeli53.kotlindesignpatternscreator.generators.FactoryGenerator
import com.intellij.openapi.project.Project
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtClass

class FactoryAction : BasePatternAction() {

    override fun isApplicable(ktClass: KtClass): Boolean {
        if (ktClass.isAnnotation() || ktClass.isEnum()) return false
        return ktClass.isInterface() || ktClass.hasModifier(KtTokens.ABSTRACT_KEYWORD)
    }

    override fun apply(project: Project, ktClass: KtClass) {
        FactoryGenerator.apply(project, ktClass)
    }
}
