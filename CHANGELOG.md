# Changelog

## 1.0.5

- The engine root no longer comes from `LUMINA_DIR`, which the engine has retired. A game solution resolves the
  engine its `.lproject` names through `EngineAssociation`, then an engine above the project, then any engine
  LuminaBuildTool has registered on this machine.

## 1.0.4

- The default build target follows the open project. With a `.lproject` present it resolves to the target
  named after it, or to the only target under the project root; otherwise it stays the engine editor. An
  explicit target in settings still wins, and clearing that field returns to following the project.
- Picked up `EditCondition` and `EditConditionHides`, so they complete, document themselves on hover, and
  stop being reported as unrecognized.

## 1.0.3

- Fixed builds against a game project failing immediately with `Project root '...lproject' does not exist`.
  LuminaBuildTool's `-Project` takes the directory holding the `.lproject` file, never the file itself; the
  plugin passed the file. Every caller now goes through `LuminaEngine.projectRoot`.

## 1.0.2

- Hover documentation is back on `REFLECT`, `PROPERTY`, `FUNCTION` and `SCRIPT_EXPORT`. 1.0.1 suppressed it on
  every macro, which was too broad: those four expand to nothing, so Rider's own popup is empty and ours is
  the only useful one. Suppression now applies to `GENERATED_BODY` and `ENTITY_SYSTEM` alone.

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
