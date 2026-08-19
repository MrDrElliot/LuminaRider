package dev.lumina.rider.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.InsertionContext
import com.intellij.codeInsight.lookup.LookupElement
import com.intellij.codeInsight.lookup.LookupElementBuilder
import dev.lumina.rider.LuminaIcons
import dev.lumina.rider.highlight.LuminaMacroScanner
import dev.lumina.rider.specifiers.LuminaSpecifier
import dev.lumina.rider.specifiers.LuminaSpecifierForm

class LuminaSpecifierCompletionContributor : CompletionContributor() {

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        val file = parameters.originalFile.virtualFile ?: return
        if (file.extension?.lowercase() !in SUPPORTED_EXTENSIONS) return

        val editor = parameters.editor
        val text = editor.document.immutableCharSequence
        val offset = editor.caretModel.offset
        val context = LuminaMacroScanner.completionContextAt(text, offset) ?: return

        val sink = result.withPrefixMatcher(text.subSequence(context.prefixStart, offset).toString())

        for (specifier in context.target.specifiers) {
            sink.addElement(lookupElement(specifier))
        }

        // Nothing else belongs in a specifier list, so C++ completion here would only be noise.
        result.stopHere()
    }

    private fun lookupElement(specifier: LuminaSpecifier): LookupElement =
        LookupElementBuilder.create(specifier.name)
            .withIcon(LuminaIcons.Lumina)
            .withTypeText(specifier.consumer.displayName, true)
            .withTailText("  ${specifier.form.displayName}", true)
            .withInsertHandler { context, _ -> appendAssignment(context, specifier) }

    /** A Value specifier is meaningless bare, so complete it straight into `Name = `. */
    private fun appendAssignment(context: InsertionContext, specifier: LuminaSpecifier) {
        if (specifier.form != LuminaSpecifierForm.VALUE) return

        val document = context.document
        val text = document.immutableCharSequence
        val insertAt = context.tailOffset

        var after = insertAt
        while (after < text.length && (text[after] == ' ' || text[after] == '\t')) after++
        if (after < text.length && text[after] == '=') return

        document.insertString(insertAt, " = ")
        context.editor.caretModel.moveToOffset(insertAt + 3)
    }

    private companion object {
        val SUPPORTED_EXTENSIONS = setOf("h", "hpp", "hxx", "hh", "inl", "ipp", "cpp", "cxx", "cc", "c")
    }
}
