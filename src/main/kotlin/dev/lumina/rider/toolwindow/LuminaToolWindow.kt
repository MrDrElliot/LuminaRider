package dev.lumina.rider.toolwindow

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.DoubleClickListener
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.ContentFactory
import dev.lumina.rider.engine.LuminaEngine
import dev.lumina.rider.engine.LuminaTargets
import dev.lumina.rider.run.LuminaMode
import dev.lumina.rider.run.LuminaRunner
import java.awt.event.MouseEvent
import javax.swing.DefaultListModel

class LuminaToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        toolWindow.contentManager.addContent(
            contentFactory.createContent(LuminaToolWindowPanel(project), "Targets", false)
        )
        toolWindow.contentManager.addContent(
            contentFactory.createContent(LuminaSpecifiersPanel(), "Specifiers", false)
        )
    }

    override fun shouldBeAvailable(project: Project): Boolean = LuminaEngine.engineRoot(project) != null
}

class LuminaToolWindowPanel(private val project: Project) : SimpleToolWindowPanel(false, true) {

    private val model = DefaultListModel<String>()
    private val targetList = JBList(model)

    init {
        targetList.emptyText.text = "No Lumina targets were found"

        object : DoubleClickListener() {
            override fun onDoubleClick(event: MouseEvent): Boolean {
                val target = targetList.selectedValue ?: return false
                LuminaRunner.run(project, LuminaMode.BUILD, target)
                return true
            }
        }.installOn(targetList)

        setToolbar(createToolbar())
        setContent(JBScrollPane(targetList))
        refresh()
    }

    private fun createToolbar(): javax.swing.JComponent {
        val group = DefaultActionGroup()
        (ActionManager.getInstance().getAction(TOOL_WINDOW_GROUP) as? ActionGroup)?.let { group.add(it) }
        group.addSeparator()
        group.add(RefreshAction())

        val toolbar = ActionManager.getInstance().createActionToolbar("LuminaToolWindow", group, true)
        toolbar.targetComponent = this
        return toolbar.component
    }

    private fun refresh() {
        ApplicationManager.getApplication().executeOnPooledThread {
            val engineRoot = LuminaEngine.engineRoot(project)
            val projectRoot = LuminaEngine.projectRoot(project)
            val targets = LuminaTargets.discover(engineRoot, projectRoot)

            ApplicationManager.getApplication().invokeLater {
                if (project.isDisposed) return@invokeLater
                val selected = targetList.selectedValue
                model.clear()
                targets.forEach(model::addElement)
                if (selected != null && targets.contains(selected)) {
                    targetList.setSelectedValue(selected, true)
                }
            }
        }
    }

    private inner class RefreshAction : AnAction("Refresh Targets", "Rescan the engine for build targets", AllIcons.Actions.Refresh), DumbAware {

        override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

        override fun actionPerformed(event: AnActionEvent) = refresh()
    }

    private companion object {
        const val TOOL_WINDOW_GROUP = "Lumina.ToolWindowActions"
    }
}
