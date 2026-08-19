package dev.lumina.rider.engine

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.SystemInfo
import dev.lumina.rider.settings.LuminaSettings
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.isRegularFile
import kotlin.io.path.name

object LuminaEngine {

    const val DEFAULT_TARGET = "Lumina"
    const val PROJECT_EXTENSION = "lproject"

    val CONFIGURATIONS = listOf("Debug", "Development", "Shipping")
    val PLATFORMS = listOf("Windows64", "Linux64")
    val TARGET_TYPES = listOf("Editor", "Game", "Program")

    fun hostPlatform(): String = if (SystemInfo.isWindows) "Windows64" else "Linux64"

    fun isEngineRoot(path: Path?): Boolean {
        if (path == null) return false
        return path.resolve("Engine/Tools/LuminaBuildTool/LuminaBuildTool.csproj").isRegularFile()
    }

    fun buildScript(engineRoot: Path): Path =
        engineRoot.resolve(if (SystemInfo.isWindows) "LuminaBuild.bat" else "LuminaBuild.sh")

    fun engineRoot(project: Project): Path? {
        val configured = LuminaSettings.getInstance(project).state.engineRoot
        if (!configured.isNullOrBlank()) {
            val path = runCatching { Path.of(configured) }.getOrNull()
            if (isEngineRoot(path)) return path
        }
        return detectEngineRoot(project)
    }

    fun detectEngineRoot(project: Project): Path? {
        val base = project.basePath?.let { runCatching { Path.of(it) }.getOrNull() }
        ancestors(base).firstOrNull(::isEngineRoot)?.let { return it }

        val fromEnvironment = System.getenv("LUMINA_DIR")?.let { runCatching { Path.of(it) }.getOrNull() }
        if (isEngineRoot(fromEnvironment)) return fromEnvironment

        val fromProjectFile = detectProjectFile(project)?.parent
        ancestors(fromProjectFile).firstOrNull(::isEngineRoot)?.let { return it }

        return null
    }

    fun projectFile(project: Project): Path? {
        val configured = LuminaSettings.getInstance(project).state.projectFile
        if (!configured.isNullOrBlank()) {
            val path = runCatching { Path.of(configured) }.getOrNull()
            if (path != null && path.isRegularFile()) return path
        }
        return detectProjectFile(project)
    }

    private fun detectProjectFile(project: Project): Path? {
        val base = project.basePath?.let { runCatching { Path.of(it) }.getOrNull() } ?: return null
        for (directory in ancestors(base)) {
            findProjectFileIn(directory)?.let { return it }
        }
        return null
    }

    private fun findProjectFileIn(directory: Path): Path? {
        if (!directory.isDirectory()) return null
        return runCatching {
            Files.list(directory).use { stream ->
                stream.filter { it.isRegularFile() && it.name.endsWith(".$PROJECT_EXTENSION", ignoreCase = true) }
                    .findFirst()
                    .orElse(null)
            }
        }.getOrNull()
    }

    private fun ancestors(start: Path?): Sequence<Path> =
        generateSequence(start?.toAbsolutePath()?.normalize()) { it.parent }.take(24)
}
