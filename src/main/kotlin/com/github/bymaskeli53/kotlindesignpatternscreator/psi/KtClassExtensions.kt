package com.github.bymaskeli53.kotlindesignpatternscreator.psi

import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtObjectDeclaration
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.psiUtil.children

fun KtClass.primaryConstructorProperties(): List<KtParameter> =
    primaryConstructorParameters.filter { it.hasValOrVar() }

fun KtClass.functions(): List<KtNamedFunction> =
    body?.children?.filterIsInstance<KtNamedFunction>() ?: emptyList()

fun KtClass.companionOrNull(): KtObjectDeclaration? =
    companionObjects.firstOrNull()

fun KtClass.hasNestedClassNamed(name: String): Boolean =
    declarations.any { it is KtClass && it.name == name }

fun KtClass.getOrCreateBody(factory: KtPsiFactory): KtClassBody {
    body?.let { return it }
    val empty = factory.createEmptyClassBody()
    return add(empty) as KtClassBody
}
