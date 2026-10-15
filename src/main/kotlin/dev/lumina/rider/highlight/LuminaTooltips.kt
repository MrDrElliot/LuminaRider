package dev.lumina.rider.highlight

import com.intellij.openapi.util.text.StringUtil
import dev.lumina.rider.specifiers.LuminaSpecifier
import dev.lumina.rider.specifiers.LuminaSpecifiers
import dev.lumina.rider.specifiers.LuminaSpecifierTarget

object LuminaTooltips {

    private val MACRO_DOCUMENTATION = mapOf(
        "REFLECT" to "Registers the type with the reflection system. The body needs GENERATED_BODY(), and the " +
            "header needs its matching .generated.h include.",
        "PROPERTY" to "Reflects the field that follows, making it visible to serialization, the editor and script.",
        "FUNCTION" to "Reflects the member function that follows.",
        "SCRIPT_EXPORT" to "Binds a free function into a C# static class. Requires Class."
    )

    fun forHighlight(highlight: LuminaHighlight): String? = when (highlight.kind) {
        LuminaHighlightKind.MACRO -> macroTooltip(highlight.macro)
        LuminaHighlightKind.SPECIFIER, LuminaHighlightKind.META_KEY -> specifierTooltip(highlight)
        LuminaHighlightKind.UNKNOWN_SPECIFIER -> unknownTooltip(highlight)
        LuminaHighlightKind.META_VALUE -> null
    }

    fun plainUnknownMessage(highlight: LuminaHighlight): String? {
        val target = LuminaSpecifierTarget.forMacro(highlight.macro) ?: return null
        val name = highlight.name ?: return null
        return LuminaSpecifiers.unknownSpecifierMessage(target, name)
    }

    /**
     * Only the macros that expand to nothing. Rider shows its own expansion preview for GENERATED_BODY and
     * ENTITY_SYSTEM, and a second popup beside it is noise.
     */
    private fun macroTooltip(macro: String): String? {
        if (LuminaSpecifierTarget.forMacro(macro) == null) return null
        return MACRO_DOCUMENTATION[macro]?.let { html("<b>$macro</b>", null, it) }
    }

    private fun specifierTooltip(highlight: LuminaHighlight): String? {
        val target = LuminaSpecifierTarget.forMacro(highlight.macro) ?: return null
        val name = highlight.name ?: return null
        val specifier: LuminaSpecifier = LuminaSpecifiers.find(target, name) ?: return null

        val subtitle = "${specifier.form.displayName}, read by the ${specifier.consumer.displayName} " +
            "(${specifier.consumer.description})"

        return html("<b>${highlight.macro}(${specifier.name})</b>", subtitle, specifier.documentation)
    }

    private fun unknownTooltip(highlight: LuminaHighlight): String? {
        val message = plainUnknownMessage(highlight) ?: return null
        return html("<b>${highlight.name}</b>", null, message)
    }

    private fun html(title: String, subtitle: String?, body: String): String {
        val builder = StringBuilder("<html><body style='width: 420px'>")
        builder.append(title)
        if (subtitle != null) {
            builder.append("<br><i>").append(StringUtil.escapeXmlEntities(subtitle)).append("</i>")
        }
        builder.append("<br><br>").append(StringUtil.escapeXmlEntities(body))
        builder.append("</body></html>")
        return builder.toString()
    }
}
