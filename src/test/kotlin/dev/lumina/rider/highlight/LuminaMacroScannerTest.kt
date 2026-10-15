package dev.lumina.rider.highlight

import dev.lumina.rider.specifiers.LuminaSpecifierTarget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LuminaMacroScannerTest {

    private fun scan(text: String) = LuminaMacroScanner.scan(text)

    private fun textOf(source: String, highlight: LuminaHighlight) =
        source.substring(highlight.start, highlight.end)

    @Test
    fun `macro name and bare specifier are highlighted`() {
        val source = "REFLECT(Component)"
        val highlights = scan(source)

        assertEquals(2, highlights.size)
        assertEquals(LuminaHighlightKind.MACRO, highlights[0].kind)
        assertEquals("REFLECT", textOf(source, highlights[0]))
        assertEquals(LuminaHighlightKind.SPECIFIER, highlights[1].kind)
        assertEquals("Component", textOf(source, highlights[1]))
    }

    @Test
    fun `key and value are separated`() {
        val source = """PROPERTY(Editable, Category = "Physics")"""
        val highlights = scan(source)

        assertEquals(4, highlights.size)
        assertEquals(LuminaHighlightKind.SPECIFIER, highlights[1].kind)
        assertEquals(LuminaHighlightKind.META_KEY, highlights[2].kind)
        assertEquals("Category", textOf(source, highlights[2]))
        assertEquals(LuminaHighlightKind.META_VALUE, highlights[3].kind)
        assertEquals(""""Physics"""", textOf(source, highlights[3]))
    }

    @Test
    fun `unknown specifier is reported against the macro's own table`() {
        val source = "PROPERTY(Editible)"
        val highlights = scan(source)

        val specifier = highlights.last()
        assertEquals(LuminaHighlightKind.UNKNOWN_SPECIFIER, specifier.kind)
        assertEquals("Editible", specifier.name)

        val message = LuminaTooltips.plainUnknownMessage(specifier)
        assertTrue(message!!.contains("Did you mean 'Editable'?"), message)
    }

    @Test
    fun `a specifier valid on another macro is still unknown here`() {
        val highlights = scan("FUNCTION(Editable)")
        assertEquals(LuminaHighlightKind.UNKNOWN_SPECIFIER, highlights.last().kind)
    }

    @Test
    fun `function and script export tables are recognised`() {
        assertEquals(LuminaHighlightKind.SPECIFIER, scan("FUNCTION(SuppressGCTransition)").last().kind)
        assertEquals(LuminaHighlightKind.META_KEY, scan("""SCRIPT_EXPORT(Class = "Math")""").first { it.kind != LuminaHighlightKind.MACRO }.kind)
    }

    @Test
    fun `macro definitions are skipped`() {
        val source = """
            #define REFLECT(...)
            #define PROPERTY(...)
        """.trimIndent()

        assertTrue(scan(source).isEmpty())
    }

    @Test
    fun `commented out and quoted macros are skipped`() {
        val source = """
            // PROPERTY(Editable)
            /* REFLECT(Component) */
            const char* Text = "PROPERTY(Editable)";
        """.trimIndent()

        assertTrue(scan(source).isEmpty())
    }

    @Test
    fun `nested parentheses do not split arguments`() {
        val source = "ENTITY_SYSTEM(RequiresUpdate(EUpdateStage::PrePhysics), RequiresUpdate(EUpdateStage::Paused))"
        val highlights = scan(source)

        assertEquals(3, highlights.size)
        assertEquals("RequiresUpdate", textOf(source, highlights[1]))
        assertEquals("RequiresUpdate", textOf(source, highlights[2]))
    }

    @Test
    fun `entity system arguments are never reported as unknown`() {
        assertTrue(scan("ENTITY_SYSTEM(RequiresUpdate(EUpdateStage::PrePhysics))").none {
            it.kind == LuminaHighlightKind.UNKNOWN_SPECIFIER
        })
    }

    @Test
    fun `empty argument list produces only the macro`() {
        val highlights = scan("GENERATED_BODY()")
        assertEquals(1, highlights.size)
        assertEquals(LuminaHighlightKind.MACRO, highlights[0].kind)
    }

    @Test
    fun `an identifier ending in a macro name is not a macro`() {
        assertTrue(scan("EASTL_DEFAULT_SORT_FUNCTION(first, last)").isEmpty())
        assertTrue(scan("MY_PROPERTY(Editable)").isEmpty())
    }

    @Test
    fun `a macro without an argument list is ignored`() {
        assertTrue(scan("int PROPERTY = 4;").isEmpty())
    }

    @Test
    fun `raw strings do not swallow the rest of the file`() {
        val source = """
            const char* Shader = R"(PROPERTY(Editable))";
            PROPERTY(Editable)
        """.trimIndent()

        val highlights = scan(source)
        assertEquals(2, highlights.size)
        assertEquals(LuminaHighlightKind.MACRO, highlights[0].kind)
    }

    @Test
    fun `a real header shape scans end to end`() {
        val source = """
            #pragma once
            #include "Weapon.generated.h"

            namespace Lumina
            {
                REFLECT(Component, Category = "Gameplay")
                struct RUNTIME_API SWeaponComponent
                {
                    GENERATED_BODY()

                    PROPERTY(Editable, Category = "Weapon", ClampMin = 0.0f, Units = "m")
                    float Range = 25.0f;
                };
            }
        """.trimIndent()

        val highlights = scan(source)
        assertTrue(highlights.none { it.kind == LuminaHighlightKind.UNKNOWN_SPECIFIER })
        assertEquals(3, highlights.count { it.kind == LuminaHighlightKind.MACRO })
        assertEquals(4, highlights.count { it.kind == LuminaHighlightKind.META_KEY })
    }

    @Test
    fun `highlight lookup finds the token under an offset`() {
        val source = "PROPERTY(Editable)"
        val highlights = scan(source)

        assertEquals("Editable", LuminaMacroScanner.highlightAt(highlights, source.indexOf("Editable"))?.name)
        assertNull(LuminaMacroScanner.highlightAt(highlights, source.length - 1))
    }

    private fun contextAt(source: String): LuminaSpecifierTarget? {
        val offset = source.indexOf('|')
        require(offset >= 0) { "mark the caret with |" }
        val text = source.removeRange(offset, offset + 1)
        return LuminaMacroScanner.completionContextAt(text, offset)?.target
    }

    @Test
    fun `completion offers the enclosing macro's table`() {
        assertEquals(LuminaSpecifierTarget.PROPERTY, contextAt("PROPERTY(|)"))
        assertEquals(LuminaSpecifierTarget.REFLECT, contextAt("REFLECT(|)"))
        assertEquals(LuminaSpecifierTarget.FUNCTION, contextAt("FUNCTION(|)"))
        assertEquals(LuminaSpecifierTarget.SCRIPT_EXPORT, contextAt("SCRIPT_EXPORT(|)"))
    }

    @Test
    fun `completion works part way through a name and after a comma`() {
        assertEquals(LuminaSpecifierTarget.PROPERTY, contextAt("PROPERTY(Edit|)"))
        assertEquals(LuminaSpecifierTarget.PROPERTY, contextAt("PROPERTY(Editable, |)"))
        assertEquals(LuminaSpecifierTarget.PROPERTY, contextAt("""PROPERTY(Category = "X", Edit|)"""))
    }

    @Test
    fun `the prefix stops at the identifier start`() {
        val source = "PROPERTY(Editable, Cat)"
        val offset = source.indexOf("Cat") + 3
        val context = LuminaMacroScanner.completionContextAt(source, offset)
        assertEquals("Cat", source.substring(context!!.prefixStart, offset))
    }

    @Test
    fun `no completion on the value side of an assignment`() {
        assertNull(contextAt("""PROPERTY(Category = |)"""))
        assertNull(contextAt("""PROPERTY(Category = "Phys|")"""))
        assertNull(contextAt("PROPERTY(ClampMin = 0.|)"))
    }

    @Test
    fun `no completion outside a macro argument list`() {
        assertNull(contextAt("PROPERTY(Editable)|"))
        assertNull(contextAt("|PROPERTY(Editable)"))
        assertNull(contextAt("int Value| = 4;"))
        assertNull(contextAt("SomeCall(Edit|)"))
    }

    @Test
    fun `macros without a specifier table offer nothing`() {
        assertNull(contextAt("GENERATED_BODY(|)"))
        assertNull(contextAt("ENTITY_SYSTEM(Requires|)"))
    }

    @Test
    fun `completion still resolves inside a real header`() {
        val source = """
            #pragma once

            namespace Lumina
            {
                REFLECT(Component)
                struct SThing
                {
                    GENERATED_BODY()

                    PROPERTY(Editable, Cat|)
                    float Value = 0.0f;
                };
            }
        """.trimIndent()

        assertEquals(LuminaSpecifierTarget.PROPERTY, contextAt(source))
    }

    @Test
    fun `an unterminated argument list still completes`() {
        assertEquals(LuminaSpecifierTarget.PROPERTY, contextAt("PROPERTY(Edit|"))
    }

    @Test
    fun `hover explains the macros that expand to nothing and defers on the ones that do not`() {
        fun macroTooltip(source: String) =
            LuminaTooltips.forHighlight(scan(source).first { it.kind == LuminaHighlightKind.MACRO })

        assertNotNull(macroTooltip("PROPERTY(Editable)"))
        assertNotNull(macroTooltip("REFLECT()"))
        assertNotNull(macroTooltip("FUNCTION()"))
        assertNotNull(macroTooltip("""SCRIPT_EXPORT(Class = "Math")"""))

        // Rider shows a real expansion preview for these two, so a second popup beside it is noise.
        assertNull(macroTooltip("GENERATED_BODY()"))
        assertNull(macroTooltip("ENTITY_SYSTEM(RequiresUpdate(EUpdateStage::PrePhysics))"))
    }
}
