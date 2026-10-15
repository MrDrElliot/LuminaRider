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

        val projectFile = detectProjectFile(project)
        associatedEngine(projectFile)?.let { return it }
        ancestors(projectFile?.parent).firstOrNull(::isEngineRoot)?.let { return it }

        return registeredEngines().values.firstOrNull()
    }

    // LuminaBuildTool records every engine checkout here, keyed by the id a .lproject's EngineAssociation names.
    private fun registeredEngines(): Map<String, Path> {
        val base = if (SystemInfo.isWindows) System.getenv("LOCALAPPDATA")
            else System.getenv("XDG_DATA_HOME") ?: "${System.getProperty("user.home")}/.local/share"
        val store = base?.let { runCatching { Path.of(it, "Lumina", "Engines.json") }.getOrNull() }
        val text = store?.takeIf { it.isRegularFile() }?.let { runCatching { Files.readString(it) }.getOrNull() } ?: return emptyMap()
        return JSON_STRING_PAIR.findAll(text).mapNotNull { match ->
            val root = runCatching { Path.of(unescapeJson(match.groupValues[2])) }.getOrNull()
            if (isEngineRoot(root)) match.groupValues[1] to root!! else null
        }.toMap()
    }

    // An engine id from this machine, or an engine root relative to the project.
    private fun associatedEngine(projectFile: Path?): Path? {
        if (projectFile == null) return null
        val text = runCatching { Files.readString(projectFile) }.getOrNull() ?: return null
        val association = JSON_STRING_PAIR.findAll(text)
            .firstOrNull { it.groupValues[1] == "EngineAssociation" }
            ?.let { unescapeJson(it.groupValues[2]) } ?: return null
        registeredEngines()[association]?.let { return it }
        return runCatching { projectFile.parent.resolve(association).normalize() }.getOrNull()?.takeIf(::isEngineRoot)
    }

    private val JSON_STRING_PAIR = Regex("\"([^\"]+)\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"")

    private fun unescapeJson(value: String): String = value.replace("\\\\", "\\").replace("\\/", "/")

    fun projectFile(project: Project): Path? {
        val configured = LuminaSettings.getInstance(project).state.projectFile
        if (!configured.isNullOrBlank()) {
            val path = runCatching { Path.of(configured) }.getOrNull()
            if (path != null && path.isRegularFile()) return path
        }
        return detectProjectFile(project)
    }

    /**
     * The directory holding the .lproject file. LuminaBuildTool's -Project takes this directory, never the
     * file itself, and rejects a file path with "Project root '...' does not exist".
     */
    fun projectRoot(project: Project): Path? = projectFile(project)?.parent?.takeIf { it.isDirectory() }

    /** The configured target, or the one the open .lproject implies. */
    fun defaultTarget(project: Project): String =
        LuminaSettings.getInstance(project).state.defaultTarget?.takeIf { it.isNotBlank() } ?: derivedTarget(project)

    /**
     * A game solution should build its own target, not the engine editor. Prefers the target named after the
     * .lproject file, since that is what the project template generates, and falls back to the only other
     * target under the project root.
     */
    fun derivedTarget(project: Project): String =
        derivedTarget(projectRoot(project), projectFile(project)?.name?.substringBeforeLast('.'))

    fun derivedTarget(projectRoot: Path?, projectName: String?): String {
        if (projectRoot == null) return DEFAULT_TARGET

        val targets = LuminaTargets.discover(null, projectRoot)

        return targets.firstOrNull { it.equals(projectName, ignoreCase = true) }
            ?: targets.singleOrNull()
            ?: DEFAULT_TARGET
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
