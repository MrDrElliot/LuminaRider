package dev.lumina.rider.highlight

import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.hint.HintManagerImpl
import com.intellij.codeInsight.hint.HintUtil
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseEventArea
import com.intellij.openapi.editor.event.EditorMouseMotionListener
import com.intellij.openapi.editor.ex.RangeHighlighterEx
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.HighlighterTargetArea
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.LightweightHint
import com.intellij.util.Alarm
import dev.lumina.rider.settings.LuminaSettings
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

class LuminaEditorFactoryListener : EditorFactoryListener {

    override fun editorCreated(event: EditorFactoryEvent) = LuminaMacroHighlighting.install(event.editor)

    override fun editorReleased(event: EditorFactoryEvent) = LuminaMacroHighlighting.uninstall(event.editor)
}

object LuminaMacroHighlighting {

    private const val REFRESH_DELAY_MILLIS = 250
    private const val HOVER_DELAY_MILLIS = 400

    private val SUPPORTED_EXTENSIONS = setOf("h", "hpp", "hxx", "hh", "inl", "ipp", "cpp", "cxx", "cc", "c")

    private val SESSION_KEY = Key.create<Session>("lumina.macro.highlighting.session")

    private val sessions: MutableSet<Session> = Collections.newSetFromMap(ConcurrentHashMap())

    fun install(editor: Editor) {
        if (editor.getUserData(SESSION_KEY) != null) return
        val project = editor.project ?: return
        val file = FileDocumentManager.getInstance().getFile(editor.document) ?: return
        if (!isSupported(file)) return

        val session = Session(editor, project)
        editor.putUserData(SESSION_KEY, session)
        sessions.add(session)
        session.schedule()
    }

    fun uninstall(editor: Editor) {
        val session = editor.getUserData(SESSION_KEY) ?: return
        editor.putUserData(SESSION_KEY, null)
        sessions.remove(session)
        Disposer.dispose(session)
    }

    fun refreshAll() {
        sessions.forEach { it.schedule() }
    }

    private fun isSupported(file: VirtualFile): Boolean = file.extension?.lowercase() in SUPPORTED_EXTENSIONS

    private class Session(private val editor: Editor, private val project: Project) : Disposable, DocumentListener {

        private val refreshAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)
        private val hoverAlarm = Alarm(Alarm.ThreadToUse.SWING_THREAD, this)
        private val highlighters = ArrayList<RangeHighlighter>()

        private var highlights: List<LuminaHighlight> = emptyList()
        private var currentHint: LightweightHint? = null
        private var hintedHighlight: LuminaHighlight? = null

        private val mouseMotionListener = object : EditorMouseMotionListener {
            override fun mouseMoved(event: EditorMouseEvent) = onMouseMoved(event)
        }

        init {
            editor.document.addDocumentListener(this, this)
            editor.addEditorMouseMotionListener(mouseMotionListener)
        }

        override fun documentChanged(event: DocumentEvent) {
            hideHint()
            schedule()
        }

        fun schedule() {
            if (refreshAlarm.isDisposed) return
            refreshAlarm.cancelAllRequests()
            refreshAlarm.addRequest({ refresh() }, REFRESH_DELAY_MILLIS, ModalityState.any())
        }

        private fun refresh() {
            if (editor.isDisposed || project.isDisposed) return

            if (!LuminaSettings.getInstance(project).state.highlightMacros) {
                clear()
                return
            }

            val document = editor.document
            val text = document.immutableCharSequence
            val stamp = document.modificationStamp

            ApplicationManager.getApplication().executeOnPooledThread {
                val scanned = LuminaMacroScanner.scan(text)
                ApplicationManager.getApplication().invokeLater({
                    if (editor.isDisposed || refreshAlarm.isDisposed) return@invokeLater
                    if (editor.document.modificationStamp != stamp) return@invokeLater
                    apply(scanned)
                }, ModalityState.any())
            }
        }

        private fun apply(scanned: List<LuminaHighlight>) {
            clear()

            val warnUnknown = LuminaSettings.getInstance(project).state.warnUnknownSpecifiers
            val length = editor.document.textLength

            highlights = scanned

            for (highlight in scanned) {
                if (highlight.start < 0 || highlight.end > length || highlight.start >= highlight.end) continue

                val unknown = highlight.kind == LuminaHighlightKind.UNKNOWN_SPECIFIER
                if (unknown && !warnUnknown) {
                    highlighters.add(add(LuminaColors.SPECIFIER, highlight))
                    continue
                }

                val added = add(LuminaColors.keyFor(highlight.kind), highlight)
                if (unknown) {
                    (added as? RangeHighlighterEx)?.errorStripeTooltip = LuminaTooltips.plainUnknownMessage(highlight)
                }
                highlighters.add(added)
            }
        }

        private fun add(key: com.intellij.openapi.editor.colors.TextAttributesKey, highlight: LuminaHighlight) =
            editor.markupModel.addRangeHighlighter(
                key,
                highlight.start,
                highlight.end,
                HighlighterLayer.ADDITIONAL_SYNTAX,
                HighlighterTargetArea.EXACT_RANGE
            )

        private fun clear() {
            if (editor.isDisposed) {
                highlighters.clear()
                highlights = emptyList()
                return
            }
            val markup = editor.markupModel
            highlighters.forEach { runCatching { markup.removeHighlighter(it) } }
            highlighters.clear()
            highlights = emptyList()
        }

        private fun onMouseMoved(event: EditorMouseEvent) {
            if (!LuminaSettings.getInstance(project).state.showSpecifierTooltips) return
            if (event.area != EditorMouseEventArea.EDITING_AREA || !event.isOverText) {
                hideHint()
                return
            }

            val highlight = LuminaMacroScanner.highlightAt(highlights, event.offset)
            if (highlight == null) {
                hideHint()
                return
            }

            if (highlight == hintedHighlight && currentHint?.isVisible == true) return

            hideHint()
            if (hoverAlarm.isDisposed) return
            hoverAlarm.cancelAllRequests()
            hoverAlarm.addRequest({ showHint(highlight) }, HOVER_DELAY_MILLIS, ModalityState.any())
        }

        private fun showHint(highlight: LuminaHighlight) {
            if (editor.isDisposed) return
            val text = LuminaTooltips.forHighlight(highlight) ?: return

            val hint = LightweightHint(HintUtil.createInformationLabel(text))
            val position = editor.offsetToLogicalPosition(highlight.start)
            val point = HintManagerImpl.getHintPosition(hint, editor, position, HintManager.ABOVE)

            HintManagerImpl.getInstanceImpl().showEditorHint(
                hint,
                editor,
                point,
                HintManager.HIDE_BY_ANY_KEY or HintManager.HIDE_BY_TEXT_CHANGE or HintManager.HIDE_BY_SCROLLING,
                0,
                false
            )

            currentHint = hint
            hintedHighlight = highlight
        }

        private fun hideHint() {
            if (!hoverAlarm.isDisposed) hoverAlarm.cancelAllRequests()
            currentHint?.hide()
            currentHint = null
            hintedHighlight = null
        }

        override fun dispose() {
            editor.removeEditorMouseMotionListener(mouseMotionListener)
            hideHint()
            clear()
        }
    }
}
