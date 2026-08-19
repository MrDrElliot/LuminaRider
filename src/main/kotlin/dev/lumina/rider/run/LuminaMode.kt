package dev.lumina.rider.run

enum class LuminaMode(
    val displayName: String,
    val cliMode: String,
    val requiresTarget: Boolean,
    val acceptsTarget: Boolean = true
) {
    BUILD("Build", "Build", true),
    REBUILD("Rebuild", "Build", true),
    REGENERATE_REFLECTION("Regenerate Reflection", "Build", true),
    RUN("Run", "Run", true),
    CLEAN("Clean", "Clean", false),
    GENERATE_PROJECT_FILES("Generate Project Files", "GenerateProjectFiles", false, acceptsTarget = false),
    SETUP("Setup Dependencies", "Setup", false, acceptsTarget = false),
    QUERY("Query", "Query", false),
    INCLUDES("Include Report", "Includes", true),
    DEPS("Dependency Report", "Deps", true);

    companion object {
        fun of(id: String?): LuminaMode = entries.firstOrNull { it.name == id } ?: BUILD
    }
}
