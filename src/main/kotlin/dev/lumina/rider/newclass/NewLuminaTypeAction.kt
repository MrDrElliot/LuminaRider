package dev.lumina.rider.newclass

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import dev.lumina.rider.engine.LuminaEngine
import javax.swing.JComponent

class NewLuminaTypeAction : AnAction(), DumbAware {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val project = event.project
        event.presentation.isEnabledAndVisible =
            project != null && LuminaEngine.engineRoot(project) != null && targetDirectory(event) != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val directory = targetDirectory(event) ?: return

        val dialog = NewLuminaTypeDialog(project)
        if (!dialog.showAndGet()) return

        val request = dialog.request()
        WriteCommandAction.runWriteCommandAction(project, "Create Lumina Type", null, {
            val header = createFile(directory, request.fileName + ".h", LuminaTypeGenerator.header(request))
            if (request.createSource) {
                createFile(directory, request.fileName + ".cpp", LuminaTypeGenerator.source(request))
            }
            FileEditorManager.getInstance(project).openFile(header, true)
        })
    }

    private fun createFile(directory: VirtualFile, name: String, content: String): VirtualFile {
        val existing = directory.findChild(name)
        val file = existing ?: directory.createChildData(this, name)
        VfsUtil.saveText(file, content)
        return file
    }

    private fun targetDirectory(event: AnActionEvent): VirtualFile? {
        val selected = event.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
        return if (selected.isDirectory) selected else selected.parent
    }
}

private class NewLuminaTypeDialog(project: Project) : DialogWrapper(project) {

    private var kind: LuminaTypeKind = LuminaTypeKind.OBJECT
    private var name: String = ""
    private var fileName: String = ""
    private var namespaceName: String = "Lumina"
    private var apiMacro: String = "RUNTIME_API"
    private var category: String = "Gameplay"
    private var createSource: Boolean = true

    init {
        title = "New Lumina Type"
        init()
    }

    private val dialogPanel by lazy { createPanel() }

    override fun createCenterPanel(): JComponent = dialogPanel

    private fun createPanel() = panel {
        row("Kind:") {
            comboBox(LuminaTypeKind.entries.toList())
                .applyToComponent { renderer = SimpleListCellRenderer.create { label, value, _ -> label.text = value?.displayName.orEmpty() } }
                .bindItem({ kind }, { kind = it ?: LuminaTypeKind.OBJECT })
        }
        row("Name:") {
            textField()
                .columns(30)
                .bindText({ name }, { name = it.trim() })
                .focused()
                .comment("The conventional prefix is added when it is missing.")
        }
        row("File name:") {
            textField()
                .columns(30)
                .bindText({ fileName }, { fileName = it.trim() })
                .comment("Leave empty to use the name without its prefix.")
        }
        row("Namespace:") {
            textField().columns(30).bindText({ namespaceName }, { namespaceName = it.trim() })
        }
        row("API macro:") {
            textField()
                .columns(30)
                .bindText({ apiMacro }, { apiMacro = it.trim() })
                .comment("For example RUNTIME_API. Leave empty for a header-only type.")
        }
        row("Category:") {
            textField().columns(30).bindText({ category }, { category = it.trim() })
        }
        row {
            checkBox("Create a matching .cpp").bindSelected({ createSource }, { createSource = it })
        }
    }

    override fun doValidate(): ValidationInfo? {
        dialogPanel.apply()
        if (!IDENTIFIER.matches(name)) {
            return ValidationInfo("Enter a valid C++ identifier.")
        }
        if (fileName.isNotBlank() && !IDENTIFIER.matches(fileName)) {
            return ValidationInfo("The file name must be a valid C++ identifier.")
        }
        return null
    }

    override fun doOKAction() {
        dialogPanel.apply()
        super.doOKAction()
    }

    fun request(): LuminaNewTypeRequest {
        val typeName = if (name.startsWith(kind.prefix) && name.length > kind.prefix.length) name else kind.prefix + name
        val resolvedFileName = fileName.ifBlank { typeName.removePrefix(kind.prefix) }
        return LuminaNewTypeRequest(
            kind = kind,
            typeName = typeName,
            fileName = resolvedFileName,
            namespaceName = namespaceName.ifBlank { "Lumina" },
            apiMacro = apiMacro,
            category = category.ifBlank { "Gameplay" },
            createSource = createSource && kind.generatesSource
        )
    }

    private companion object {
        val IDENTIFIER = Regex("[A-Za-z_][A-Za-z0-9_]*")
    }
}
