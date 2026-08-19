package dev.lumina.rider.run

import com.intellij.execution.filters.Filter
import com.intellij.execution.filters.OpenFileHyperlinkInfo
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.io.File

class LuminaOutputFilter(private val project: Project) : Filter {

    override fun applyFilter(line: String, entireLength: Int): Filter.Result? {
        val msvc = MSVC.find(line)
        val match = msvc ?: CLANG.find(line) ?: return null

        val pathGroup = match.groups[1] ?: return null
        val lineGroup = match.groups[2] ?: return null
        val columnGroup = match.groups[3]

        val file = LocalFileSystem.getInstance().findFileByIoFile(File(pathGroup.value.trim())) ?: return null
        val lineNumber = lineGroup.value.toIntOrNull()?.minus(1) ?: return null
        val column = columnGroup?.value?.toIntOrNull()?.minus(1) ?: 0

        val lineStart = entireLength - line.length
        val linkStart = lineStart + pathGroup.range.first
        val lastNumberEnd = (columnGroup?.range?.last ?: lineGroup.range.last) + 1
        val linkEnd = lineStart + if (msvc != null) lastNumberEnd + 1 else lastNumberEnd

        return Filter.Result(linkStart, linkEnd, OpenFileHyperlinkInfo(project, file, lineNumber, column))
    }

    private companion object {
        val MSVC = Regex("""([A-Za-z]:[^:*?"<>|\r\n]+?)\((\d+)(?:,(\d+))?\)\s*:\s*(?:fatal\s+)?(?:error|warning)\b""")
        val CLANG = Regex("""((?:[A-Za-z]:)?[^\s:*?"<>|][^:*?"<>|\r\n]*?):(\d+):(\d+):\s*(?:fatal\s+)?(?:error|warning)\b""")
    }
}
