package dev.lumina.rider.run

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import dev.lumina.rider.engine.LuminaEngine
import dev.lumina.rider.engine.LuminaTargets
import javax.swing.JComponent

class LuminaSettingsEditor(private val project: Project) : SettingsEditor<LuminaRunConfiguration>() {

    private class EditorState {
        var mode: LuminaMode = LuminaMode.BUILD
        var target: String = LuminaEngine.DEFAULT_TARGET
        var buildConfiguration: String = "Development"
        var platform: String = ""
        var targetType: String = "Editor"
        var arguments: String = ""
        var programArguments: String = ""
        var verbose: Boolean = false
    }

    private val state = EditorState()

    private val targets: List<String> by lazy {
        val discovered = LuminaTargets.discover(LuminaEngine.engineRoot(project), LuminaEngine.projectFile(project)?.parent)
        if (discovered.isEmpty()) listOf(LuminaEngine.DEFAULT_TARGET) else discovered
    }

    private val dialogPanel: DialogPanel by lazy { createPanel() }

    private fun createPanel(): DialogPanel = panel {
        row("Mode:") {
            comboBox(LuminaMode.entries.toList())
                .applyToComponent { renderer = SimpleListCellRenderer.create { label, value, _ -> label.text = value?.displayName.orEmpty() } }
                .bindItem({ state.mode }, { state.mode = it ?: LuminaMode.BUILD })
        }
        row("Target:") {
            comboBox(targets)
                .applyToComponent { isEditable = true }
                .bindItem({ state.target }, { state.target = it.orEmpty().trim() })
        }
        row("Configuration:") {
            comboBox(LuminaEngine.CONFIGURATIONS)
                .bindItem({ state.buildConfiguration }, { state.buildConfiguration = it ?: "Development" })
        }
        row("Target type:") {
            comboBox(LuminaEngine.TARGET_TYPES)
                .bindItem({ state.targetType }, { state.targetType = it ?: "Editor" })
        }
        row("Platform:") {
            comboBox(listOf("") + LuminaEngine.PLATFORMS)
                .bindItem({ state.platform }, { state.platform = it.orEmpty() })
                .comment("Empty uses the host platform.")
        }
        row("Build tool arguments:") {
            textField()
                .columns(44)
                .bindText({ state.arguments }, { state.arguments = it })
                .comment("For example -NoUnity -KeepGoing -Tracy=off")
        }
        row("Program arguments:") {
            textField()
                .columns(44)
                .bindText({ state.programArguments }, { state.programArguments = it })
                .comment("Passed after -- to the launched executable. Run mode only.")
        }
        row {
            checkBox("Verbose build tool output")
                .bindSelected({ state.verbose }, { state.verbose = it })
        }
    }

    override fun createEditor(): JComponent = dialogPanel

    override fun resetEditorFrom(configuration: LuminaRunConfiguration) {
        val options = configuration.options
        state.mode = LuminaMode.of(options.mode)
        state.target = options.target.ifBlank { LuminaEngine.DEFAULT_TARGET }
        state.buildConfiguration = options.buildConfiguration
        state.platform = options.platform
        state.targetType = options.targetType
        state.arguments = options.arguments
        state.programArguments = options.programArguments
        state.verbose = options.verbose
        dialogPanel.reset()
    }

    override fun applyEditorTo(configuration: LuminaRunConfiguration) {
        dialogPanel.apply()
        val options = configuration.options
        options.mode = state.mode.name
        options.target = state.target
        options.buildConfiguration = state.buildConfiguration
        options.platform = state.platform
        options.targetType = state.targetType
        options.arguments = state.arguments
        options.programArguments = state.programArguments
        options.verbose = state.verbose
    }
}
