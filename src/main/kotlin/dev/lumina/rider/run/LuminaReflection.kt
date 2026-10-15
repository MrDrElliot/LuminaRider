package dev.lumina.rider.run

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import dev.lumina.rider.engine.LuminaEngine
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory

object LuminaReflection {

    private val LOG = logger<LuminaReflection>()

    private val GENERATED_DIRECTORIES = listOf("Intermediates/Reflection", "Intermediates/CSharpBindings")

    fun outputRoot(project: Project): Path? =
        LuminaEngine.projectRoot(project) ?: LuminaEngine.engineRoot(project)

    fun deleteGeneratedCode(project: Project) {
        val root = outputRoot(project) ?: return
        for (relative in GENERATED_DIRECTORIES) {
            delete(root.resolve(relative))
        }
    }

    private fun delete(directory: Path) {
        if (!directory.isDirectory()) return
        runCatching {
            Files.walk(directory).use { stream ->
                stream.sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) }
            }
        }.onFailure { LOG.warn("Failed to delete $directory", it) }
    }
}
