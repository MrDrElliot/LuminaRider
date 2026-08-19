package dev.lumina.rider.engine

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name

object LuminaTargets {

    private const val SUFFIX = ".Target.cs"
    private const val MAX_DEPTH = 8

    private val SKIPPED = setOf(
        "Intermediates", "Binaries", "External", "Saved", "obj", "bin",
        ".git", ".vs", ".idea", ".claude", "node_modules", "ThirdParty", "Logs"
    )

    fun discover(engineRoot: Path?, projectRoot: Path?): List<String> {
        val roots = listOfNotNull(
            engineRoot?.resolve("Engine"),
            engineRoot?.resolve("Plugins"),
            projectRoot?.resolve("Source"),
            projectRoot?.resolve("Plugins")
        ).filter { it.isDirectory() }

        val names = sortedSetOf<String>(String.CASE_INSENSITIVE_ORDER)
        for (root in roots) {
            collect(root, names)
        }
        return names.toList()
    }

    private fun collect(root: Path, into: MutableSet<String>) {
        runCatching {
            Files.walk(root, MAX_DEPTH).use { stream ->
                stream.filter { candidate ->
                    candidate.name.endsWith(SUFFIX) && candidate.none { segment -> segment.name in SKIPPED }
                }.forEach { candidate ->
                    val name = candidate.name.removeSuffix(SUFFIX)
                    if (!name.startsWith("$")) {
                        into.add(name)
                    }
                }
            }
        }
    }
}
