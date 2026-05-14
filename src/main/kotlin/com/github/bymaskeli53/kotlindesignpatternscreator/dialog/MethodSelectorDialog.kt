package com.github.bymaskeli53.kotlindesignpatternscreator.dialog

import com.github.bymaskeli53.kotlindesignpatternscreator.PluginBundle
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import org.jetbrains.kotlin.psi.KtNamedFunction
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.DefaultListModel
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.ListSelectionModel

class MethodSelectorDialog(
    project: Project,
    private val functions: List<KtNamedFunction>
) : DialogWrapper(project, true) {

    private val list: JBList<String>

    init {
        title = PluginBundle["dialog.strategy.title"]
        val model = DefaultListModel<String>().apply {
            functions.forEach { addElement(it.signatureLabel()) }
        }
        list = JBList(model).apply {
            selectionMode = ListSelectionModel.SINGLE_SELECTION
            addListSelectionListener { isOKActionEnabled = !isSelectionEmpty }
        }
        init()
        isOKActionEnabled = false
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(0, 8))
        panel.preferredSize = Dimension(420, 280)
        panel.add(JBLabel(PluginBundle["dialog.strategy.label"]), BorderLayout.NORTH)
        panel.add(JBScrollPane(list), BorderLayout.CENTER)
        return panel
    }

    fun selectedFunction(): KtNamedFunction? {
        val idx = list.selectedIndex
        return if (idx in functions.indices) functions[idx] else null
    }

    private fun KtNamedFunction.signatureLabel(): String {
        val params = valueParameters.joinToString(", ") { p ->
            val type = p.typeReference?.text ?: "?"
            "${p.name ?: "_"}: $type"
        }
        val ret = typeReference?.text?.let { ": $it" } ?: ""
        return "${name ?: "<unnamed>"}($params)$ret"
    }
}
