package dev.lumina.rider.highlight

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.PlainSyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import dev.lumina.rider.LuminaIcons
import javax.swing.Icon

class LuminaColorSettingsPage : ColorSettingsPage {

    override fun getDisplayName(): String = "Lumina Engine"

    override fun getIcon(): Icon = LuminaIcons.Lumina

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getHighlighter(): SyntaxHighlighter = PlainSyntaxHighlighter()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> = TAGS

    override fun getDemoText(): String = DEMO_TEXT

    private companion object {

        val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Reflection macro", LuminaColors.MACRO),
            AttributesDescriptor("Specifier", LuminaColors.SPECIFIER),
            AttributesDescriptor("Metadata key", LuminaColors.META_KEY),
            AttributesDescriptor("Metadata value", LuminaColors.META_VALUE),
            AttributesDescriptor("Unrecognized specifier", LuminaColors.UNKNOWN_SPECIFIER)
        )

        val TAGS = mapOf(
            "macro" to LuminaColors.MACRO,
            "specifier" to LuminaColors.SPECIFIER,
            "key" to LuminaColors.META_KEY,
            "value" to LuminaColors.META_VALUE,
            "unknown" to LuminaColors.UNKNOWN_SPECIFIER
        )

        val DEMO_TEXT = """
            #pragma once

            #include "Core/Object/Object.h"
            #include "Weapon.generated.h"

            namespace Lumina
            {
                <macro>REFLECT</macro>(<specifier>Component</specifier>, <key>Category</key> = <value>"Gameplay"</value>)
                struct RUNTIME_API SWeaponComponent
                {
                    <macro>GENERATED_BODY</macro>()

                    <macro>PROPERTY</macro>(<specifier>Editable</specifier>, <key>Category</key> = <value>"Weapon"</value>, <key>ClampMin</key> = <value>0.0f</value>, <key>Units</key> = <value>"m"</value>)
                    float Range = 25.0f;

                    <macro>PROPERTY</macro>(<specifier>Editable</specifier>, <specifier>Replicated</specifier>, <specifier>ScriptReadOnly</specifier>)
                    int32 Ammo = 30;

                    <macro>PROPERTY</macro>(<unknown>Editible</unknown>)
                    bool bDrawn = false;
                };

                <macro>REFLECT</macro>()
                class RUNTIME_API CWeapon : public CObject
                {
                    <macro>GENERATED_BODY</macro>()

                public:

                    <macro>FUNCTION</macro>(<specifier>SuppressGCTransition</specifier>)
                    void Fire();
                };
            }
        """.trimIndent()
    }
}
