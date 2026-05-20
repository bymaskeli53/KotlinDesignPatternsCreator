package com.github.bymaskeli53.kotlindesignpatternscreator.dialog

import com.github.bymaskeli53.kotlindesignpatternscreator.PluginBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import org.jetbrains.kotlin.psi.KtParameter
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.BoxLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel

class PropertyRequirednessDialog(
    project: Project,
    private val properties: List<KtParameter>
) : DialogWrapper(project, true) {

    private val checkBoxes: List<JCheckBox> = properties.map { p ->
        JCheckBox("${p.name ?: "_"}: ${p.typeReference?.text ?: "?"}    Required", true)
    }

    init {
        title = PluginBundle["dialog.builder.title"]
        init()
    }

    override fun createCenterPanel(): JComponent {
        val rows = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            checkBoxes.forEach { add(it) }
        }
        val panel = JPanel(BorderLayout(0, 8)).apply {
            preferredSize = Dimension(460, (checkBoxes.size * 28 + 80).coerceAtMost(420))
            add(JBLabel(PluginBundle["dialog.builder.label"]), BorderLayout.NORTH)
            add(JBScrollPane(rows), BorderLayout.CENTER)
        }
        return panel
    }

    fun isRequired(p: KtParameter): Boolean {
        val idx = properties.indexOf(p)
        return idx >= 0 && checkBoxes[idx].isSelected
    }
}
