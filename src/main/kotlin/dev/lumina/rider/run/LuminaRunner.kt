package dev.lumina.rider.run

import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManager
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.openapi.project.Project
import dev.lumina.rider.engine.LuminaEngine
import dev.lumina.rider.settings.LuminaSettings

object LuminaRunner {

    fun run(project: Project, mode: LuminaMode, target: String? = null, arguments: String = "") {
        val runManager = RunManager.getInstance(project)
        val settings = runManager.createConfiguration(displayName(mode, target), LuminaRunConfigurationType.factory())
        val configuration = settings.configuration as LuminaRunConfiguration
        val defaults = LuminaSettings.getInstance(project).state

        configuration.options.also { options ->
            options.mode = mode.name
            options.target = target ?: defaults.defaultTarget ?: LuminaEngine.DEFAULT_TARGET
            options.buildConfiguration = defaults.defaultConfiguration ?: "Development"
            options.targetType = defaults.defaultTargetType ?: "Editor"
            options.platform = defaults.defaultPlatform.orEmpty()
            options.arguments = arguments
        }

        settings.isTemporary = true
        runManager.setTemporaryConfiguration(settings)
        ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
    }

    private fun displayName(mode: LuminaMode, target: String?): String =
        if (mode.acceptsTarget && !target.isNullOrBlank()) "${mode.displayName} $target" else mode.displayName
}
