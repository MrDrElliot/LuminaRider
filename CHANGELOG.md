# Changelog

## 1.0.1

- Completion inside a reflection macro's argument list offers that macro's own specifiers, with the form and
  consumer shown per item. Value specifiers complete into `Name = `. Nothing is offered on the value side of
  an `=`, or inside GENERATED_BODY and ENTITY_SYSTEM, which have no specifier table.
- Hover no longer fires on the macro name. Rider already documents those with a real expansion preview, and
  both popups showed at once. Specifiers, metadata keys and unrecognized specifiers still explain themselves.

## 1.0.0

First release.

- Lumina run configuration driving LuminaBuildTool: Build, Rebuild, Run, Clean, Generate Project Files,
  Setup, Query, Includes, Deps and Regenerate Reflection.
- Tools menu and main toolbar actions for the same operations, plus a target picker.
- Clickable MSVC and Clang diagnostics in the run console.
- Highlighting for REFLECT, PROPERTY, FUNCTION, SCRIPT_EXPORT, GENERATED_BODY and ENTITY_SYSTEM, with their
  specifiers, metadata keys and metadata values.
- Unrecognized specifiers are marked with the same message and suggestion the Reflector prints as LRT1009.
- Hover documentation for macros and specifiers.
- Live templates and a New Lumina Type generator.
- Lumina tool window with a build target list and a searchable specifier reference.
