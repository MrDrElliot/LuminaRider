package dev.lumina.rider.settings

import com.intellij.openapi.components.BaseState
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.SimplePersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import dev.lumina.rider.engine.LuminaEngine

@Service(Service.Level.PROJECT)
@State(name = "LuminaEngineSettings", storages = [Storage("lumina.xml")])
class LuminaSettings : SimplePersistentStateComponent<LuminaSettings.State>(State()) {

    class State : BaseState() {
        var engineRoot by string()
        var projectFile by string()
        var defaultTarget by string(LuminaEngine.DEFAULT_TARGET)
        var defaultConfiguration by string("Development")
        var defaultPlatform by string("")
        var defaultTargetType by string("Editor")
        var extraArguments by string()
        var verbose by property(false)
        var highlightMacros by property(true)
        var warnUnknownSpecifiers by property(true)
        var showSpecifierTooltips by property(true)
    }

    companion object {
        fun getInstance(project: Project): LuminaSettings = project.service()
    }
}
