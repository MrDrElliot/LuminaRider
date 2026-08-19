package dev.lumina.rider.run

import com.intellij.execution.ExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.KillableColoredProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.runners.ProgramRunner
import com.intellij.execution.configurations.CommandLineState

class LuminaCommandLineState(
    environment: ExecutionEnvironment,
    private val configuration: LuminaRunConfiguration
) : CommandLineState(environment) {

    init {
        val builder = TextConsoleBuilderFactory.getInstance().createBuilder(environment.project)
        builder.addFilter(LuminaOutputFilter(environment.project))
        consoleBuilder = builder
    }

    override fun execute(executor: Executor, runner: ProgramRunner<*>): ExecutionResult {
        if (configuration.mode == LuminaMode.REGENERATE_REFLECTION) {
            LuminaReflection.deleteGeneratedCode(configuration.project)
        }
        return super.execute(executor, runner)
    }

    override fun startProcess(): ProcessHandler {
        val handler = KillableColoredProcessHandler(LuminaCommandLine.create(configuration))
        ProcessTerminatedListener.attach(handler)
        return handler
    }
}
