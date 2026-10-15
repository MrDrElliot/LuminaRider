package dev.lumina.rider.specifiers

// Generated from Engine/Applications/Reflector/Source/Reflector/ReflectionSpecifiers.h
// by tools/generate_specifiers.py. Do not edit by hand.

object LuminaSpecifierTable {

    val REFLECT: List<LuminaSpecifier> = listOf(
        LuminaSpecifier("ReflectedName", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.REFLECTOR, "Registers the type under this name instead of its C++ spelling. Required on a REFLECT'd alias or template instantiation."),
        LuminaSpecifier("MinimalAPI", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.REFLECTOR, "Exports only StaticStruct()/StaticClass() across the module boundary instead of force-exporting every member."),
        LuminaSpecifier("Component", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.RUNTIME, "Marks the struct as an ECS component. Registers component meta and opts the EnTT pool into in_place_delete."),
        LuminaSpecifier("System", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.RUNTIME, "Marks the struct as an ECS system and registers it with the system registry."),
        LuminaSpecifier("Event", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.RUNTIME, "Marks the struct as an ECS event and registers it with the event registry."),
        LuminaSpecifier("BitMask", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.RUNTIME, "Marks an enum as a set of bit flags. CEnum::IsBitmaskEnum() reports it and the editor draws checkboxes."),
        LuminaSpecifier("ConfigFile", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.RUNTIME, "Backs the class with the named config file. The config system loads and saves its properties there."),
        LuminaSpecifier("Category", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.EDITOR, "Groups the type under a named heading in the component picker and the settings list."),
        LuminaSpecifier("DisplayName", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.EDITOR, "Overrides the label shown for the type in editor UI."),
        LuminaSpecifier("ToolTip", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.EDITOR, "Hover text for the type. Filled from the declaration's doc comment when not written by hand."),
        LuminaSpecifier("HideInComponentList", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Hides the component from the scene editor's add-component list."),
        LuminaSpecifier("HideInDetails", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Hides the type from the details panel."),
        LuminaSpecifier("NotPlaceable", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Excludes the class from the node graph's placeable node registry."),
        LuminaSpecifier("Scriptable", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.SCRIPT, "Emits a C# subclassable wrapper. Every reflected virtual on the class becomes overridable from C#."),
        LuminaSpecifier("ScriptFastCalls", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.SCRIPT, "Applies SuppressGCTransition to every binding on the type. FUNCTION(NoSuppressGCTransition) opts one back out."),
        LuminaSpecifier("CSharpValueMirror", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.SCRIPT, "The type has a hand-written blittable C# value struct. The emitter marshals it by value and generates no wrapper."),
        LuminaSpecifier("NoCSharp", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.SCRIPT, "Suppresses C# binding generation for the type."),
    )

    val PROPERTY: List<LuminaSpecifier> = listOf(
        LuminaSpecifier("Getter", LuminaSpecifierForm.EITHER, LuminaSpecifierConsumer.REFLECTOR, "Routes reads through the named accessor. Defaults to Get<PropertyName> when no value is given."),
        LuminaSpecifier("Setter", LuminaSpecifierForm.EITHER, LuminaSpecifierConsumer.REFLECTOR, "Routes writes through the named accessor. Defaults to Set<PropertyName> when no value is given."),
        LuminaSpecifier("ReflectAs", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.REFLECTOR, "Reflects the field as the named type instead of its declared type."),
        LuminaSpecifier("NoSerialize", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.RUNTIME, "Excludes the property from serialization."),
        LuminaSpecifier("EditorOnly", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.RUNTIME, "The property exists for editor tooling only and is stripped from cooked packages."),
        LuminaSpecifier("Replicated", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.RUNTIME, "The property participates in network replication."),
        LuminaSpecifier("DuplicateTransient", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.RUNTIME, "Resets the property to its default when the owning object is duplicated."),
        LuminaSpecifier("StructBase", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.RUNTIME, "Constrains a bare FInstancedStruct to structs deriving from the named base."),
        LuminaSpecifier("Editable", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Shows the property in the details panel and allows editing."),
        LuminaSpecifier("ReadOnly", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Shows the property in the details panel with editing disabled. Do not combine with Editable."),
        LuminaSpecifier("Category", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.EDITOR, "Groups the property under a named heading. Nested headings separate with a pipe."),
        LuminaSpecifier("DisplayName", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.EDITOR, "Overrides the label shown for the property."),
        LuminaSpecifier("ToolTip", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.EDITOR, "Hover text for the property. Filled from the declaration's doc comment when not written by hand."),
        LuminaSpecifier("ClampMin", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.EDITOR, "Lower bound applied to a numeric property by the editor widget."),
        LuminaSpecifier("ClampMax", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.EDITOR, "Upper bound applied to a numeric property by the editor widget."),
        LuminaSpecifier("Delta", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.EDITOR, "Per-pixel drag step for a numeric property."),
        LuminaSpecifier("Units", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.EDITOR, "Unit suffix appended to a numeric property's displayed value."),
        LuminaSpecifier("Color", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Draws the numeric vector as a color swatch and picker."),
        LuminaSpecifier("NoDrag", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Disables click-drag editing on a numeric property."),
        LuminaSpecifier("Multiline", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Draws a string property as a multi-line text box."),
        LuminaSpecifier("FilePath", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Draws a string property with a file browse button."),
        LuminaSpecifier("InputAction", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Draws a string property as an input action picker."),
        LuminaSpecifier("BonePicker", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Draws a string property as a skeleton bone picker."),
        LuminaSpecifier("SocketPicker", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Draws a string property as a mesh socket picker."),
        LuminaSpecifier("CurvePicker", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Draws a string property as an animation curve picker."),
        LuminaSpecifier("ParameterPicker", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Draws a string property as a material parameter picker."),
        LuminaSpecifier("ObjectParameterPicker", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Draws a string property as an object-valued material parameter picker."),
        LuminaSpecifier("AssetType", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.EDITOR, "Restricts an asset reference picker to the named asset class."),
        LuminaSpecifier("RowType", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.EDITOR, "Restricts a data table row handle picker to the named row struct."),
        LuminaSpecifier("Entity", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Draws the property as an entity reference picker."),
        LuminaSpecifier("NoReorder", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Removes the drag handles from an array property."),
        LuminaSpecifier("NoResize", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Removes the add and remove buttons from an array property."),
        LuminaSpecifier("DefaultCollapsed", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Draws a struct or instanced struct property collapsed on first open."),
        LuminaSpecifier("EditCondition", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.EDITOR, "Disables the property while the expression is false. Terms are Prop, !Prop, Prop == Value or Prop != Value, joined by && or ||."),
        LuminaSpecifier("EditConditionHides", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Hides the property instead of disabling it while its EditCondition is false."),
        LuminaSpecifier("RequiresRecook", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.EDITOR, "Editing the property triggers a recook of the owning asset."),
        LuminaSpecifier("ScriptReadOnly", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.SCRIPT, "The generated C# wrapper emits a getter only, whatever the editor flags say."),
        LuminaSpecifier("ScriptWritable", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.SCRIPT, "The generated C# wrapper emits a setter, whatever the editor flags say."),
        LuminaSpecifier("ScriptHidden", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.SCRIPT, "Emits no C# wrapper member and no native accessor for the property."),
        LuminaSpecifier("NotScriptable", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.SCRIPT, "Alias of ScriptHidden."),
        LuminaSpecifier("SkipHotReload", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.SCRIPT, "Excludes the property from script hot-reload state migration."),
    )

    val FUNCTION: List<LuminaSpecifier> = listOf(
        LuminaSpecifier("ToolTip", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.EDITOR, "Hover text for the function. Filled from the declaration's doc comment when not written by hand."),
        LuminaSpecifier("SuppressGCTransition", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.SCRIPT, "Skips the GC transition on the generated C# call. Only valid on a short leaf function that never calls back into managed code."),
        LuminaSpecifier("NoSuppressGCTransition", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.SCRIPT, "Opts one function back out of a type-wide REFLECT(ScriptFastCalls)."),
    )

    val SCRIPT_EXPORT: List<LuminaSpecifier> = listOf(
        LuminaSpecifier("Class", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.SCRIPT, "Required. The C# static class the free function binds to, optionally namespaced."),
        LuminaSpecifier("SuppressGCTransition", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.SCRIPT, "Skips the GC transition on the generated C# call. Only valid on a short leaf function."),
    )

    val INJECTED: List<LuminaSpecifier> = listOf(
        LuminaSpecifier("Aliases", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.RUNTIME, "Prior C# type names for a script type, written by the host so renamed scripts still resolve."),
        LuminaSpecifier("ScriptTypeName", LuminaSpecifierForm.VALUE, LuminaSpecifierConsumer.RUNTIME, "The C# type name behind a script-backed struct, stable across reloads unlike its object name."),
        LuminaSpecifier("ScriptInstanceBase", LuminaSpecifierForm.FLAG, LuminaSpecifierConsumer.RUNTIME, "Marks the generated base struct of a script type so pickers offer the concrete subtypes instead."),
    )

}
