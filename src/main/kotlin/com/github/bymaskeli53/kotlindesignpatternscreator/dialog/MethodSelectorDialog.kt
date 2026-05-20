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
    private val functions: List<KtNamedFunction>,
    private val multiSelect: Boolean = false,
    titleKey: String = "dialog.strategy.title",
    private val labelKey: String = "dialog.strategy.label",
    private val allowEmptySelection: Boolean = false
) : DialogWrapper(project, true) {

    private val list: JBList<String> = JBList(DefaultListModel<String>().apply {
        functions.forEach { addElement(it.signatureLabel()) }
    }).apply {
        selectionMode = if (multiSelect)
            ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        else
            ListSelectionModel.SINGLE_SELECTION
        addListSelectionListener {
            isOKActionEnabled = allowEmptySelection || !isSelectionEmpty
        }
    }

    init {
        title = PluginBundle[titleKey]
        init()
        isOKActionEnabled = allowEmptySelection
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(0, 8))
        panel.preferredSize = Dimension(440, 300)
        panel.add(JBLabel(PluginBundle[labelKey]), BorderLayout.NORTH)
        panel.add(JBScrollPane(list), BorderLayout.CENTER)
        return panel
    }

    fun selectedFunction(): KtNamedFunction? {
        val idx = list.selectedIndex
        return if (idx in functions.indices) functions[idx] else null
    }

    fun selectedFunctions(): List<KtNamedFunction> =
        list.selectedIndices.toList().mapNotNull { idx -> functions.getOrNull(idx) }

    private fun KtNamedFunction.signatureLabel(): String {
        val params = valueParameters.joinToString(", ") { p ->
            val type = p.typeReference?.text ?: "?"
            "${p.name ?: "_"}: $type"
        }
        val ret = typeReference?.text?.let { ": $it" } ?: ""
        return "${name ?: "<unnamed>"}($params)$ret"
    }
}
