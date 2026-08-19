package dev.lumina.rider.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import dev.lumina.rider.engine.LuminaEngine
import dev.lumina.rider.engine.LuminaTargets
import dev.lumina.rider.run.LuminaMode
import dev.lumina.rider.run.LuminaRunner
import dev.lumina.rider.settings.LuminaConfigurable

abstract class LuminaAction : AnAction(), DumbAware {

    override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

    override fun update(event: AnActionEvent) {
        val project = event.project
        event.presentation.isEnabledAndVisible = project != null && LuminaEngine.engineRoot(project) != null
    }
}

abstract class LuminaModeAction(private val mode: LuminaMode, private val target: String? = null) : LuminaAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        LuminaRunner.run(project, mode, target)
    }
}

class LuminaBuildAction : LuminaModeAction(LuminaMode.BUILD)
class LuminaRebuildAction : LuminaModeAction(LuminaMode.REBUILD)
class LuminaCleanAction : LuminaModeAction(LuminaMode.CLEAN, target = "")
class LuminaRunEditorAction : LuminaModeAction(LuminaMode.RUN)
class LuminaGenerateProjectFilesAction : LuminaModeAction(LuminaMode.GENERATE_PROJECT_FILES)
class LuminaRegenerateReflectionAction : LuminaModeAction(LuminaMode.REGENERATE_REFLECTION)
class LuminaSetupAction : LuminaModeAction(LuminaMode.SETUP)
class LuminaQueryAction : LuminaModeAction(LuminaMode.QUERY, target = "")

class LuminaBuildTargetAction : LuminaAction() {

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        chooseTarget(project) { target -> LuminaRunner.run(project, LuminaMode.BUILD, target) }
    }

    private fun chooseTarget(project: Project, onChosen: (String) -> Unit) {
        val targets = LuminaTargets.discover(LuminaEngine.engineRoot(project), LuminaEngine.projectFile(project)?.parent)
        if (targets.isEmpty()) {
            onChosen(LuminaEngine.DEFAULT_TARGET)
            return
        }

        JBPopupFactory.getInstance()
            .createPopupChooserBuilder(targets)
            .setTitle("Build Lumina Target")
            .setItemChosenCallback(onChosen)
            .createPopup()
            .showCenteredInCurrentWindow(project)
    }
}

class LuminaOpenSettingsAction : LuminaAction() {

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible = event.project != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        ShowSettingsUtil.getInstance().showSettingsDialog(project, LuminaConfigurable::class.java)
    }
}
