package dev.lumina.rider.specifiers

enum class LuminaSpecifierForm(val displayName: String) {
    FLAG("flag"),
    VALUE("key = value"),
    EITHER("flag or key = value")
}

enum class LuminaSpecifierConsumer(val displayName: String, val description: String) {
    REFLECTOR("Reflector", "changes the generated C++"),
    RUNTIME("Runtime", "read through CStruct or FProperty metadata at runtime"),
    EDITOR("Editor", "drives the property grid or another editor surface only"),
    SCRIPT("Script", "shapes the generated C# binding")
}

data class LuminaSpecifier(
    val name: String,
    val form: LuminaSpecifierForm,
    val consumer: LuminaSpecifierConsumer,
    val documentation: String
)

enum class LuminaSpecifierTarget(val macro: String) {
    REFLECT("REFLECT"),
    PROPERTY("PROPERTY"),
    FUNCTION("FUNCTION"),
    SCRIPT_EXPORT("SCRIPT_EXPORT");

    val specifiers: List<LuminaSpecifier>
        get() = when (this) {
            REFLECT -> LuminaSpecifierTable.REFLECT
            PROPERTY -> LuminaSpecifierTable.PROPERTY
            FUNCTION -> LuminaSpecifierTable.FUNCTION
            SCRIPT_EXPORT -> LuminaSpecifierTable.SCRIPT_EXPORT
        }

    companion object {
        fun forMacro(macro: String): LuminaSpecifierTarget? = entries.firstOrNull { it.macro == macro }
    }
}

object LuminaSpecifiers {

    fun find(target: LuminaSpecifierTarget, name: String): LuminaSpecifier? =
        target.specifiers.firstOrNull { it.name == name } ?: LuminaSpecifierTable.INJECTED.firstOrNull { it.name == name }

    // Mirrors SuggestSpecifier in ReflectionSpecifiers.cpp so a hint here matches the LRT1009 the build prints.
    fun suggest(target: LuminaSpecifierTarget, name: String): LuminaSpecifier? {
        val maxDistance = when {
            name.length <= 4 -> 1
            name.length <= 8 -> 2
            else -> 3
        }

        var best: LuminaSpecifier? = null
        var bestDistance = maxDistance + 1

        for (specifier in target.specifiers) {
            val distance = editDistance(name, specifier.name)
            if (distance < bestDistance) {
                bestDistance = distance
                best = specifier
            }
        }

        return if (bestDistance <= maxDistance) best else null
    }

    fun unknownSpecifierMessage(target: LuminaSpecifierTarget, name: String): String {
        val suggestion = suggest(target, name)
        return if (suggestion != null) {
            "${target.macro} specifier '$name' is not recognized and will be ignored. Did you mean '${suggestion.name}'?"
        } else {
            "${target.macro} specifier '$name' is not recognized and will be ignored. " +
                "Add it to ReflectionSpecifiers.h if it is intentional."
        }
    }

    private fun editDistance(left: String, right: String): Int {
        val row = IntArray(right.length + 1) { it }

        for (i in 1..left.length) {
            var diagonal = row[0]
            row[0] = i

            for (j in 1..right.length) {
                val previous = row[j]
                val cost = if (left[i - 1] == right[j - 1]) 0 else 1
                row[j] = minOf(row[j] + 1, row[j - 1] + 1, diagonal + cost)
                diagonal = previous
            }
        }

        return row[right.length]
    }
}
