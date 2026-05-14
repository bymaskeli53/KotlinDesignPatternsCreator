package com.github.bymaskeli53.kotlindesignpatternscreator.actions

import com.github.bymaskeli53.kotlindesignpatternscreator.PluginBundle
import com.github.bymaskeli53.kotlindesignpatternscreator.psi.PsiHelper
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile

abstract class BasePatternAction : AnAction() {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val ktClass = targetClass(e)
        e.presentation.isEnabledAndVisible = ktClass != null && isApplicable(ktClass)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val ktClass = targetClass(e) ?: return
        val editor = e.getData(CommonDataKeys.EDITOR)
        runPattern(project, ktClass, editor)
    }

    protected open fun isApplicable(ktClass: KtClass): Boolean {
        if (ktClass.isInterface()) return false
        if (ktClass.isAnnotation()) return false
        if (ktClass.isEnum()) return false
        return true
    }

    protected fun targetClass(e: AnActionEvent): KtClass? {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) as? KtFile ?: return null
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return null
        val offset = editor.caretModel.offset
        val element = psiFile.findElementAt(offset) ?: return null
        return PsiTreeUtil.getParentOfType(element, KtClass::class.java)
    }

    /** Subclasses with their own UI flow (e.g. Strategy) override this. */
    protected open fun runPattern(project: Project, ktClass: KtClass, editor: Editor?) {
        runInWriteCommand(project) { apply(project, ktClass) }
    }

    /** Default implementation for write-action-only patterns. */
    protected open fun apply(project: Project, ktClass: KtClass) {}

    protected fun runInWriteCommand(project: Project, block: () -> Unit) {
        try {
            WriteCommandAction.runWriteCommandAction(project, commandName(), null, Runnable {
                block()
            })
        } catch (t: Throwable) {
            thisLogger().warn("Failed to apply ${javaClass.simpleName}", t)
            PsiHelper.notify(
                project,
                PluginBundle["error.psi", t.message ?: t.javaClass.simpleName],
                NotificationType.ERROR
            )
        }
    }

    protected open fun commandName(): String = "Apply ${javaClass.simpleName.removeSuffix("Action")} Pattern"
}
