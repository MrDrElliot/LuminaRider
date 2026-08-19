package dev.lumina.rider.highlight

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.CodeInsightColors
import com.intellij.openapi.editor.colors.TextAttributesKey

object LuminaColors {

    val MACRO: TextAttributesKey =
        TextAttributesKey.createTextAttributesKey("LUMINA_MACRO", DefaultLanguageHighlighterColors.METADATA)

    val SPECIFIER: TextAttributesKey =
        TextAttributesKey.createTextAttributesKey("LUMINA_SPECIFIER", DefaultLanguageHighlighterColors.KEYWORD)

    val META_KEY: TextAttributesKey =
        TextAttributesKey.createTextAttributesKey("LUMINA_META_KEY", DefaultLanguageHighlighterColors.INSTANCE_FIELD)

    val META_VALUE: TextAttributesKey =
        TextAttributesKey.createTextAttributesKey("LUMINA_META_VALUE", DefaultLanguageHighlighterColors.STRING)

    val UNKNOWN_SPECIFIER: TextAttributesKey =
        TextAttributesKey.createTextAttributesKey("LUMINA_UNKNOWN_SPECIFIER", CodeInsightColors.WARNINGS_ATTRIBUTES)

    fun keyFor(kind: LuminaHighlightKind): TextAttributesKey = when (kind) {
        LuminaHighlightKind.MACRO -> MACRO
        LuminaHighlightKind.SPECIFIER -> SPECIFIER
        LuminaHighlightKind.META_KEY -> META_KEY
        LuminaHighlightKind.META_VALUE -> META_VALUE
        LuminaHighlightKind.UNKNOWN_SPECIFIER -> UNKNOWN_SPECIFIER
    }
}
