package dev.lumina.rider.toolwindow

import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.DocumentAdapter
import com.intellij.ui.JBSplitter
import com.intellij.ui.SearchTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import dev.lumina.rider.specifiers.LuminaSpecifier
import dev.lumina.rider.specifiers.LuminaSpecifierTarget
import javax.swing.JEditorPane
import javax.swing.ListSelectionModel
import javax.swing.event.DocumentEvent
import javax.swing.table.AbstractTableModel

class LuminaSpecifiersPanel : SimpleToolWindowPanel(true, true) {

    private data class Row(val macro: String, val specifier: LuminaSpecifier)

    private val allRows: List<Row> = LuminaSpecifierTarget.entries.flatMap { target ->
        target.specifiers.map { Row(target.macro, it) }
    }

    private var rows: List<Row> = allRows

    private val model = object : AbstractTableModel() {
        override fun getRowCount(): Int = rows.size
        override fun getColumnCount(): Int = 4

        override fun getColumnName(column: Int): String = when (column) {
            0 -> "Specifier"
            1 -> "Macro"
            2 -> "Form"
            else -> "Read by"
        }

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val row = rows[rowIndex]
            return when (columnIndex) {
                0 -> row.specifier.name
                1 -> row.macro
                2 -> row.specifier.form.displayName
                else -> row.specifier.consumer.displayName
            }
        }

        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = false
    }

    private val table = JBTable(model)
    private val documentation = JEditorPane("text/html", "")
    private val search = SearchTextField()

    init {
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
        table.setShowGrid(false)
        table.selectionModel.addListSelectionListener { updateDocumentation() }

        documentation.isEditable = false
        documentation.background = UIUtil.getPanelBackground()
        documentation.border = JBUI.Borders.empty(8)

        search.textEditor.emptyText.text = "Filter specifiers"
        search.addDocumentListener(object : DocumentAdapter() {
            override fun textChanged(event: DocumentEvent) = applyFilter()
        })

        val splitter = JBSplitter(true, 0.65f)
        splitter.firstComponent = JBScrollPane(table)
        splitter.secondComponent = JBScrollPane(documentation)

        setToolbar(search)
        setContent(splitter)

        if (rows.isNotEmpty()) {
            table.setRowSelectionInterval(0, 0)
        }
    }

    private fun applyFilter() {
        val query = search.text.trim()
        rows = if (query.isEmpty()) {
            allRows
        } else {
            allRows.filter {
                it.specifier.name.contains(query, ignoreCase = true) ||
                    it.macro.contains(query, ignoreCase = true) ||
                    it.specifier.documentation.contains(query, ignoreCase = true)
            }
        }

        model.fireTableDataChanged()
        if (rows.isNotEmpty()) {
            table.setRowSelectionInterval(0, 0)
        } else {
            documentation.text = ""
        }
    }

    private fun updateDocumentation() {
        val index = table.selectedRow
        if (index < 0 || index >= rows.size) {
            documentation.text = ""
            return
        }

        val row = rows[index]
        documentation.text = buildString {
            append("<html><body style='font-family: sans-serif'>")
            append("<b>").append(row.macro).append("(").append(row.specifier.name).append(")</b><br>")
            append("<i>").append(row.specifier.form.displayName).append(", read by the ")
            append(row.specifier.consumer.displayName).append(" (").append(row.specifier.consumer.description)
            append(")</i><br><br>")
            append(row.specifier.documentation)
            append("</body></html>")
        }
        documentation.caretPosition = 0
    }
}
