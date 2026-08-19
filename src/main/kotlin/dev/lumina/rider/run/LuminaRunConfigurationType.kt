package dev.lumina.rider.run

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationTypeBase
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.project.Project
import dev.lumina.rider.LuminaIcons

class LuminaRunConfigurationType : ConfigurationTypeBase(
    ID,
    "Lumina",
    "Builds and runs Lumina Engine targets through LuminaBuildTool",
    LuminaIcons.Lumina
) {
    init {
        addFactory(LuminaConfigurationFactory(this))
    }

    companion object {
        const val ID = "LuminaBuildToolConfiguration"

        fun getInstance(): LuminaRunConfigurationType =
            ConfigurationTypeUtil.findConfigurationType(LuminaRunConfigurationType::class.java)

        fun factory(): ConfigurationFactory = getInstance().configurationFactories.first()
    }
}

class LuminaConfigurationFactory(type: LuminaRunConfigurationType) : ConfigurationFactory(type) {

    override fun getId(): String = "Lumina"

    override fun createTemplateConfiguration(project: Project): RunConfiguration =
        LuminaRunConfiguration(project, this, "Lumina")

    override fun getOptionsClass(): Class<out RunConfigurationOptions> = LuminaRunConfigurationOptions::class.java
}
