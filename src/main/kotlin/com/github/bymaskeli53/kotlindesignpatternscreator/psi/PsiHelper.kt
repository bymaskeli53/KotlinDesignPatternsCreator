package com.github.bymaskeli53.kotlindesignpatternscreator.psi

import com.github.bymaskeli53.kotlindesignpatternscreator.PluginBundle
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiElement
import com.intellij.psi.codeStyle.CodeStyleManager

private const val NOTIFICATION_GROUP_ID = "KotlinDesignPatternsCreator"

object PsiHelper {

    fun notify(project: Project, content: String, type: NotificationType = NotificationType.INFORMATION) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(NOTIFICATION_GROUP_ID)
            .createNotification(content, type)
            .notify(project)
    }

    fun warn(project: Project, message: String) {
        Messages.showWarningDialog(project, message, PluginBundle["warn.title"])
    }

    fun reformat(element: PsiElement) {
        CodeStyleManager.getInstance(element.project).reformat(element)
    }
}
