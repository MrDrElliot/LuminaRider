package dev.lumina.rider.run

import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import dev.lumina.rider.engine.LuminaEngine

class LuminaRunConfiguration(project: Project, factory: ConfigurationFactory, name: String) :
    RunConfigurationBase<LuminaRunConfigurationOptions>(project, factory, name) {

    public override fun getOptions(): LuminaRunConfigurationOptions =
        super.getOptions() as LuminaRunConfigurationOptions

    val mode: LuminaMode get() = LuminaMode.of(options.mode)

    override fun getConfigurationEditor(): SettingsEditor<LuminaRunConfiguration> = LuminaSettingsEditor(project)

    override fun getState(executor: Executor, environment: ExecutionEnvironment) =
        LuminaCommandLineState(environment, this)

    override fun checkConfiguration() {
        val engineRoot = LuminaEngine.engineRoot(project)
            ?: throw RuntimeConfigurationError("No Lumina engine root was found. Set one in Settings | Tools | Lumina Engine.")

        if (!LuminaEngine.buildScript(engineRoot).toFile().isFile) {
            throw RuntimeConfigurationError("${LuminaEngine.buildScript(engineRoot)} does not exist.")
        }

        if (mode.requiresTarget && options.target.isBlank()) {
            throw RuntimeConfigurationError("${mode.displayName} needs a target name.")
        }
    }
}
