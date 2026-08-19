package dev.lumina.rider.highlight

import dev.lumina.rider.specifiers.LuminaSpecifiers
import dev.lumina.rider.specifiers.LuminaSpecifierTarget

enum class LuminaHighlightKind { MACRO, SPECIFIER, UNKNOWN_SPECIFIER, META_KEY, META_VALUE }

data class LuminaHighlight(
    val start: Int,
    val end: Int,
    val kind: LuminaHighlightKind,
    val macro: String,
    val name: String? = null
)

/** One macro invocation. [contentEnd] is the closing parenthesis, or the end of the text when unterminated. */
data class LuminaMacroCall(val macro: String, val argsStart: Int, val contentEnd: Int)

data class LuminaCompletionContext(val target: LuminaSpecifierTarget, val prefixStart: Int)

object LuminaMacroScanner {

    const val MAX_LENGTH = 4_000_000

    // GENERATED_BODY and ENTITY_SYSTEM carry no specifier table, so their arguments are coloured but not checked.
    val MACROS = setOf("REFLECT", "PROPERTY", "FUNCTION", "SCRIPT_EXPORT", "GENERATED_BODY", "ENTITY_SYSTEM")

    private class ScanResult {
        val highlights = ArrayList<LuminaHighlight>()
        val calls = ArrayList<LuminaMacroCall>()
    }

    fun scan(text: CharSequence): List<LuminaHighlight> = scanInternal(text)?.highlights ?: emptyList()

    /**
     * The specifier table to complete against when [offset] sits at a specifier name inside a macro's
     * argument list, or null anywhere else. Null on the value side of an `=`, where a table is meaningless.
     */
    fun completionContextAt(text: CharSequence, offset: Int): LuminaCompletionContext? {
        if (offset !in 0..text.length) return null

        val call = scanInternal(text)?.calls?.firstOrNull { offset > it.argsStart && offset <= it.contentEnd }
            ?: return null

        val target = LuminaSpecifierTarget.forMacro(call.macro) ?: return null

        val segmentStart = segmentStartFor(text, call.argsStart, offset)
        if (findAssignment(text, segmentStart, offset) >= 0) return null

        var prefixStart = offset
        while (prefixStart > segmentStart && isIdentifierPart(text[prefixStart - 1])) prefixStart--

        return LuminaCompletionContext(target, prefixStart)
    }

    private fun scanInternal(text: CharSequence): ScanResult? {
        if (text.length > MAX_LENGTH) return null

        val result = ScanResult()
        val length = text.length
        var index = 0
        var atLineStart = true

        while (index < length) {
            val current = text[index]
            when {
                current == '\n' -> {
                    atLineStart = true
                    index++
                }

                current == ' ' || current == '\t' || current == '\r' -> index++

                current == '/' && index + 1 < length && text[index + 1] == '/' ->
                    index = skipLineComment(text, index)

                current == '/' && index + 1 < length && text[index + 1] == '*' -> {
                    index = skipBlockComment(text, index)
                    atLineStart = false
                }

                current == '#' && atLineStart -> index = skipPreprocessorLine(text, index)

                current == '"' -> {
                    index = skipString(text, index, index > 0 && text[index - 1] == 'R')
                    atLineStart = false
                }

                current == '\'' -> {
                    index = skipCharLiteral(text, index)
                    atLineStart = false
                }

                isIdentifierStart(current) -> {
                    val start = index
                    index = readIdentifier(text, index)
                    val macro = text.subSequence(start, index).toString()
                    if (macro in MACROS) {
                        val parenthesis = skipWhitespace(text, index)
                        if (parenthesis < length && text[parenthesis] == '(') {
                            result.highlights.add(
                                LuminaHighlight(start, index, LuminaHighlightKind.MACRO, macro, macro)
                            )
                            index = readArguments(text, parenthesis, macro, result.highlights)
                            // An unterminated list runs to the end of the text; a closed one stops at its ')'.
                            val closed = index > parenthesis && text[index - 1] == ')'
                            result.calls.add(LuminaMacroCall(macro, parenthesis, if (closed) index - 1 else index))
                        }
                    }
                    atLineStart = false
                }

                else -> {
                    atLineStart = false
                    index++
                }
            }
        }

        return result
    }

    fun highlightAt(highlights: List<LuminaHighlight>, offset: Int): LuminaHighlight? =
        highlights.firstOrNull { offset >= it.start && offset < it.end }

    private fun readArguments(
        text: CharSequence,
        openParenthesis: Int,
        macro: String,
        out: MutableList<LuminaHighlight>
    ): Int {
        val length = text.length
        var index = openParenthesis + 1
        var depth = 1
        var segmentStart = index

        while (index < length) {
            val current = text[index]
            when {
                current == '/' && index + 1 < length && text[index + 1] == '/' -> index = skipLineComment(text, index)
                current == '/' && index + 1 < length && text[index + 1] == '*' -> index = skipBlockComment(text, index)
                current == '"' -> index = skipString(text, index, index > 0 && text[index - 1] == 'R')
                current == '\'' -> index = skipCharLiteral(text, index)

                current == '(' -> {
                    depth++
                    index++
                }

                current == ')' -> {
                    depth--
                    if (depth == 0) {
                        emitSegment(text, segmentStart, index, macro, out)
                        return index + 1
                    }
                    index++
                }

                current == ',' && depth == 1 -> {
                    emitSegment(text, segmentStart, index, macro, out)
                    index++
                    segmentStart = index
                }

                else -> index++
            }
        }

        emitSegment(text, segmentStart, length, macro, out)
        return length
    }

    private fun segmentStartFor(text: CharSequence, argsStart: Int, offset: Int): Int {
        var index = argsStart + 1
        var depth = 1
        var segmentStart = index

        while (index < offset) {
            val current = text[index]
            when {
                current == '/' && index + 1 < offset && text[index + 1] == '/' -> index = skipLineComment(text, index)
                current == '/' && index + 1 < offset && text[index + 1] == '*' -> index = skipBlockComment(text, index)
                current == '"' -> index = skipString(text, index, index > 0 && text[index - 1] == 'R')
                current == '\'' -> index = skipCharLiteral(text, index)

                current == '(' -> {
                    depth++
                    index++
                }

                current == ')' -> {
                    depth--
                    if (depth == 0) return segmentStart
                    index++
                }

                current == ',' && depth == 1 -> {
                    index++
                    segmentStart = index
                }

                else -> index++
            }
        }

        return segmentStart
    }

    private fun emitSegment(
        text: CharSequence,
        rawStart: Int,
        rawEnd: Int,
        macro: String,
        out: MutableList<LuminaHighlight>
    ) {
        var start = rawStart
        var end = rawEnd
        while (start < end && text[start].isWhitespace()) start++
        while (end > start && text[end - 1].isWhitespace()) end--
        if (start >= end) return

        val target = LuminaSpecifierTarget.forMacro(macro)
        val assignment = findAssignment(text, start, end)

        if (assignment < 0) {
            val identifierEnd = leadingIdentifierEnd(text, start, end) ?: return
            val name = text.subSequence(start, identifierEnd).toString()
            out.add(LuminaHighlight(start, identifierEnd, classify(target, name, bare = true), macro, name))
            return
        }

        var keyEnd = assignment
        while (keyEnd > start && text[keyEnd - 1].isWhitespace()) keyEnd--

        leadingIdentifierEnd(text, start, keyEnd)?.let { identifierEnd ->
            val name = text.subSequence(start, identifierEnd).toString()
            out.add(LuminaHighlight(start, identifierEnd, classify(target, name, bare = false), macro, name))
        }

        var valueStart = assignment + 1
        while (valueStart < end && text[valueStart].isWhitespace()) valueStart++
        if (valueStart < end) {
            out.add(LuminaHighlight(valueStart, end, LuminaHighlightKind.META_VALUE, macro))
        }
    }

    private fun classify(target: LuminaSpecifierTarget?, name: String, bare: Boolean): LuminaHighlightKind {
        val known = target == null || LuminaSpecifiers.find(target, name) != null
        return when {
            !known -> LuminaHighlightKind.UNKNOWN_SPECIFIER
            bare -> LuminaHighlightKind.SPECIFIER
            else -> LuminaHighlightKind.META_KEY
        }
    }

    private fun leadingIdentifierEnd(text: CharSequence, start: Int, end: Int): Int? {
        if (start >= end || !isIdentifierStart(text[start])) return null
        var identifierEnd = start
        while (identifierEnd < end && isIdentifierPart(text[identifierEnd])) identifierEnd++
        return identifierEnd
    }

    private fun findAssignment(text: CharSequence, start: Int, end: Int): Int {
        var depth = 0
        var index = start
        while (index < end) {
            val current = text[index]
            when {
                current == '"' -> {
                    index = skipString(text, index, index > start && text[index - 1] == 'R')
                    continue
                }

                current == '\'' -> {
                    index = skipCharLiteral(text, index)
                    continue
                }

                current == '(' || current == '[' -> depth++
                current == ')' || current == ']' -> depth--

                current == '=' && depth == 0 -> {
                    val previous = if (index > start) text[index - 1] else ' '
                    val next = if (index + 1 < end) text[index + 1] else ' '
                    val comparison = previous == '=' || previous == '!' || previous == '<' ||
                        previous == '>' || next == '='
                    if (!comparison) return index
                }
            }
            index++
        }
        return -1
    }

    private fun skipWhitespace(text: CharSequence, from: Int): Int {
        var index = from
        while (index < text.length && text[index].isWhitespace()) index++
        return index
    }

    private fun skipLineComment(text: CharSequence, from: Int): Int {
        var index = from + 2
        while (index < text.length && text[index] != '\n') index++
        return index
    }

    private fun skipBlockComment(text: CharSequence, from: Int): Int {
        var index = from + 2
        while (index + 1 < text.length) {
            if (text[index] == '*' && text[index + 1] == '/') return index + 2
            index++
        }
        return text.length
    }

    private fun skipPreprocessorLine(text: CharSequence, from: Int): Int {
        val length = text.length
        var index = from
        while (index < length) {
            val current = text[index]
            if (current == '\\') {
                var lookahead = index + 1
                while (lookahead < length && (text[lookahead] == ' ' || text[lookahead] == '\t' || text[lookahead] == '\r')) {
                    lookahead++
                }
                if (lookahead < length && text[lookahead] == '\n') {
                    index = lookahead + 1
                    continue
                }
            }
            if (current == '\n') return index
            index++
        }
        return length
    }

    private fun skipString(text: CharSequence, quote: Int, raw: Boolean): Int {
        if (raw) return skipRawString(text, quote)

        val length = text.length
        var index = quote + 1
        while (index < length) {
            when (text[index]) {
                '\\' -> index += 2
                '"' -> return index + 1
                '\n' -> return index
                else -> index++
            }
        }
        return length
    }

    private fun skipRawString(text: CharSequence, quote: Int): Int {
        val length = text.length
        var index = quote + 1
        while (index < length && text[index] != '(' && index - quote <= 17) index++
        if (index >= length || text[index] != '(') return quote + 1

        val terminator = ")" + text.subSequence(quote + 1, index).toString() + "\""
        var search = index
        while (search + terminator.length <= length) {
            if (matchesAt(text, search, terminator)) return search + terminator.length
            search++
        }
        return length
    }

    private fun matchesAt(text: CharSequence, at: Int, value: String): Boolean {
        for (offset in value.indices) {
            if (text[at + offset] != value[offset]) return false
        }
        return true
    }

    private fun skipCharLiteral(text: CharSequence, quote: Int): Int {
        val length = text.length
        var index = quote + 1
        while (index < length) {
            when (text[index]) {
                '\\' -> index += 2
                '\'' -> return index + 1
                '\n' -> return index
                else -> index++
            }
        }
        return length
    }

    private fun readIdentifier(text: CharSequence, from: Int): Int {
        var index = from
        while (index < text.length && isIdentifierPart(text[index])) index++
        return index
    }

    private fun isIdentifierStart(character: Char): Boolean = character == '_' || character.isLetter()

    private fun isIdentifierPart(character: Char): Boolean = character == '_' || character.isLetterOrDigit()
}
