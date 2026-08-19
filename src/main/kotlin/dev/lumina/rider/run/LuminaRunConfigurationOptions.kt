package dev.lumina.rider.run

import com.intellij.execution.configurations.RunConfigurationOptions
import com.intellij.openapi.components.StoredProperty

class LuminaRunConfigurationOptions : RunConfigurationOptions() {

    private val modeProperty: StoredProperty<String?> = string(LuminaMode.BUILD.name).provideDelegate(this, "mode")
    private val targetProperty: StoredProperty<String?> = string("").provideDelegate(this, "target")
    private val configurationProperty: StoredProperty<String?> = string("Development").provideDelegate(this, "buildConfiguration")
    private val platformProperty: StoredProperty<String?> = string("").provideDelegate(this, "platform")
    private val targetTypeProperty: StoredProperty<String?> = string("Editor").provideDelegate(this, "targetType")
    private val argumentsProperty: StoredProperty<String?> = string("").provideDelegate(this, "arguments")
    private val programArgumentsProperty: StoredProperty<String?> = string("").provideDelegate(this, "programArguments")
    private val verboseProperty: StoredProperty<Boolean> = property(false).provideDelegate(this, "verbose")

    var mode: String
        get() = modeProperty.getValue(this) ?: LuminaMode.BUILD.name
        set(value) = modeProperty.setValue(this, value)

    var target: String
        get() = targetProperty.getValue(this).orEmpty()
        set(value) = targetProperty.setValue(this, value)

    var buildConfiguration: String
        get() = configurationProperty.getValue(this) ?: "Development"
        set(value) = configurationProperty.setValue(this, value)

    var platform: String
        get() = platformProperty.getValue(this).orEmpty()
        set(value) = platformProperty.setValue(this, value)

    var targetType: String
        get() = targetTypeProperty.getValue(this) ?: "Editor"
        set(value) = targetTypeProperty.setValue(this, value)

    var arguments: String
        get() = argumentsProperty.getValue(this).orEmpty()
        set(value) = argumentsProperty.setValue(this, value)

    var programArguments: String
        get() = programArgumentsProperty.getValue(this).orEmpty()
        set(value) = programArgumentsProperty.setValue(this, value)

    var verbose: Boolean
        get() = verboseProperty.getValue(this)
        set(value) = verboseProperty.setValue(this, value)
}
