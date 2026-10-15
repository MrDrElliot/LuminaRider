package dev.lumina.rider.settings

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toMutableProperty
import dev.lumina.rider.engine.LuminaEngine
import dev.lumina.rider.highlight.LuminaMacroHighlighting

class LuminaConfigurable(private val project: Project) : BoundConfigurable("Lumina Engine") {

    override fun createPanel(): DialogPanel {
        val state = LuminaSettings.getInstance(project).state
        val detected = LuminaEngine.detectEngineRoot(project)

        return panel {
            group("Engine") {
                row("Engine root:") {
                    textFieldWithBrowseButton(
                        FileChooserDescriptorFactory.createSingleFolderDescriptor()
                            .withTitle("Select Lumina Engine Root")
                    )
                        .columns(48)
                        .bindText({ state.engineRoot.orEmpty() }, { state.engineRoot = it.trim().ifEmpty { null } })
                        .comment(
                            if (detected != null) "Leave empty to use the detected root: $detected"
                            else "No engine root was detected next to this solution. Set one here."
                        )
                }
                row("Project file:") {
                    textFieldWithBrowseButton(
                        FileChooserDescriptorFactory.createSingleFileDescriptor(LuminaEngine.PROJECT_EXTENSION)
                            .withTitle("Select Lumina Project File")
                    )
                        .columns(48)
                        .bindText({ state.projectFile.orEmpty() }, { state.projectFile = it.trim().ifEmpty { null } })
                        .comment("Its directory is passed as -Project when building a game against an installed engine.")
                }
            }

            group("Build defaults") {
                row("Target:") {
                    textField()
                        .columns(24)
                        .bindText({ state.defaultTarget.orEmpty() }, { state.defaultTarget = it.trim().ifEmpty { null } })
                        .comment(
                            "Leave empty to follow the open project, which here means " +
                                "<b>${LuminaEngine.derivedTarget(project)}</b>."
                        )
                }
                row("Configuration:") {
                    comboBox(LuminaEngine.CONFIGURATIONS)
                        .bindItem(state::defaultConfiguration.toMutableProperty())
                }
                row("Target type:") {
                    comboBox(LuminaEngine.TARGET_TYPES)
                        .bindItem(state::defaultTargetType.toMutableProperty())
                }
                row("Platform:") {
                    comboBox(listOf("") + LuminaEngine.PLATFORMS)
                        .bindItem(state::defaultPlatform.toMutableProperty())
                        .comment("Empty uses the host platform.")
                }
                row("Extra arguments:") {
                    textField()
                        .columns(48)
                        .bindText({ state.extraArguments.orEmpty() }, { state.extraArguments = it.trim().ifEmpty { null } })
                        .comment("Appended to every LuminaBuildTool invocation, for example -Tracy=off.")
                }
                row {
                    checkBox("Pass -Verbose").bindSelected(state::verbose)
                }
            }

            group("Editor") {
                row {
                    checkBox("Highlight REFLECT, PROPERTY and FUNCTION macros")
                        .bindSelected(state::highlightMacros)
                }
                row {
                    checkBox("Mark specifiers the Reflector does not recognize")
                        .bindSelected(state::warnUnknownSpecifiers)
                        .comment("Mirrors the LRT1009 warning the Reflector prints during a build.")
                }
                row {
                    checkBox("Show specifier documentation on hover")
                        .bindSelected(state::showSpecifierTooltips)
                }
            }
        }
    }

    override fun apply() {
        super.apply()
        LuminaMacroHighlighting.refreshAll()
    }
}
