package dev.lumina.rider.run

import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.util.execution.ParametersListUtil
import com.intellij.openapi.util.SystemInfo
import dev.lumina.rider.engine.LuminaEngine
import dev.lumina.rider.settings.LuminaSettings
import java.nio.charset.StandardCharsets

object LuminaCommandLine {

    private val MODES_WITH_BUILD_FLAGS = setOf(
        LuminaMode.BUILD, LuminaMode.REBUILD, LuminaMode.REGENERATE_REFLECTION,
        LuminaMode.RUN, LuminaMode.CLEAN, LuminaMode.QUERY, LuminaMode.INCLUDES, LuminaMode.DEPS
    )

    fun create(configuration: LuminaRunConfiguration): GeneralCommandLine {
        val project = configuration.project
        val options = configuration.options
        val mode = configuration.mode

        val engineRoot = LuminaEngine.engineRoot(project)
            ?: throw ExecutionException("No Lumina engine root was found. Set one in Settings | Tools | Lumina Engine.")

        val script = LuminaEngine.buildScript(engineRoot)
        if (!script.toFile().isFile) {
            throw ExecutionException("$script does not exist.")
        }

        val commandLine = GeneralCommandLine()
        if (SystemInfo.isWindows) {
            commandLine.exePath = "cmd.exe"
            commandLine.addParameters("/c", script.toString())
        } else {
            commandLine.exePath = script.toString()
        }

        commandLine.addParameter(mode.cliMode)

        if (mode.acceptsTarget && options.target.isNotBlank()) {
            commandLine.addParameter(options.target)
        }

        if (mode in MODES_WITH_BUILD_FLAGS) {
            commandLine.addParameter("-Configuration=${options.buildConfiguration}")
            commandLine.addParameter("-TargetType=${options.targetType}")

            if (options.platform.isNotBlank()) {
                commandLine.addParameter("-Platform=${options.platform}")
            }
        }

        if (mode == LuminaMode.REBUILD) {
            commandLine.addParameter("-Clean")
        }

        LuminaEngine.projectFile(project)?.let { commandLine.addParameter("-Project=$it") }

        val settings = LuminaSettings.getInstance(project).state
        settings.extraArguments?.let { commandLine.addParameters(ParametersListUtil.parse(it)) }
        commandLine.addParameters(ParametersListUtil.parse(options.arguments))

        if (options.verbose || settings.verbose) {
            commandLine.addParameter("-Verbose")
        }

        if (mode == LuminaMode.RUN && options.programArguments.isNotBlank()) {
            commandLine.addParameter("--")
            commandLine.addParameters(ParametersListUtil.parse(options.programArguments))
        }

        commandLine.setWorkDirectory(engineRoot.toFile())
        commandLine.charset = StandardCharsets.UTF_8
        commandLine.withParentEnvironmentType(GeneralCommandLine.ParentEnvironmentType.CONSOLE)

        return commandLine
    }
}
