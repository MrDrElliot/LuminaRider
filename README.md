# Lumina Engine for Rider

A JetBrains Rider plugin for the [Lumina Engine](https://github.com/MrDrElliot/LuminaEngine): build and run
targets through `LuminaBuildTool`, regenerate reflection code, and read reflected C++ with the reflection
macros highlighted and checked.

## Features

### Build and run

A **Lumina** run configuration type drives `LuminaBuild.bat` / `LuminaBuild.sh` with a mode, target,
configuration, target type, platform, build tool arguments and program arguments. Modes map onto the build
tool's own: Build, Rebuild (`-Clean`), Run, Clean, Generate Project Files, Setup, Query, Includes, Deps, plus
Regenerate Reflection.

The same operations are available as actions under **Tools | Lumina** and from the **Lumina** group in the
main toolbar, using the per-solution defaults from settings. Compiler diagnostics in the console are clickable
for both MSVC (`file(line,col): error C2065:`) and Clang (`file:line:col: error:`) output.

**Regenerate Reflection** deletes `Intermediates/Reflection` and `Intermediates/CSharpBindings` under the
output root, then builds, so the Reflector runs from scratch. That invalidates every generated header, so the
build after it is a long one.

### Editor

`REFLECT`, `PROPERTY`, `FUNCTION`, `SCRIPT_EXPORT`, `GENERATED_BODY` and `ENTITY_SYSTEM` are highlighted along
with their specifiers, metadata keys and metadata values. Colors live under
**Settings | Editor | Color Scheme | Lumina Engine**.

Specifiers are checked against the Reflector's own table. An unrecognized specifier is underlined with the
same wording and spelling suggestion the build prints as LRT1009, using the same edit-distance thresholds, so
the editor and the build agree. Hovering a macro or specifier shows what it does, whether it is a flag or a
key/value pair, and which layer reads it.

Completion inside `REFLECT`, `PROPERTY`, `FUNCTION` and `SCRIPT_EXPORT` offers exactly the specifiers that
macro accepts, each labelled with its form and the layer that reads it. A specifier taking a value completes
into `Name = `. Nothing is offered on the value side of an `=`, or inside `GENERATED_BODY` and
`ENTITY_SYSTEM`, which have no table.

Live templates (`lclass`, `lstruct`, `lcomp`, `lsystem`, `lenum`, `lprop`, `lpropr`, `lfunc`, `lgen`) expand in
C and C++ files. **New | Lumina Type** generates a reflected header and, where the kind needs one, its source
file.

### Tool window

The **Lumina** tool window has a Targets tab listing every `*.Target.cs` found in the engine and the solution
(double-click to build), and a Specifiers tab: a searchable reference for every REFLECT, PROPERTY, FUNCTION and
SCRIPT_EXPORT specifier with its form, its consumer and its documentation.

## Settings

**Settings | Tools | Lumina Engine** holds the engine root, the `.lproject` file, the default target,
configuration, target type and platform, extra build tool arguments, and the editor toggles.

The default target follows the open project when left empty: with a `.lproject` present it resolves to the
target named after it, or to the only target under the project root, and otherwise to the engine editor. Fill
the field in to pin a target instead.

The engine root is detected automatically: first from the solution directory and its ancestors (looking for
`Engine/Tools/LuminaBuildTool/LuminaBuildTool.csproj`), then from the engine the `.lproject` names, then from the
directory holding the `.lproject` file, then from any engine LuminaBuildTool has registered on this machine. Set it explicitly when the plugin cannot find it.

## Building

Requires JDK 21. The Gradle wrapper pulls everything else, including the Rider SDK.

```bash
./gradlew buildPlugin
```

The installable archive lands in `build/distributions/`. Install it with **Settings | Plugins | Install Plugin
from Disk**.

To try it in a sandbox IDE:

```bash
./gradlew runIde
```

To run the scanner unit tests, and the JetBrains plugin verifier against the recommended IDE versions:

```bash
./gradlew test verifyPlugin
```

## Regenerating the specifier table

`src/main/kotlin/dev/lumina/rider/specifiers/LuminaSpecifiers.kt` is generated from the engine's
`ReflectionSpecifiers.h`. Re-run the generator whenever a specifier is added, removed or re-documented there:

```bash
python tools/generate_specifiers.py H:/LuminaEngine
```

The Kotlin lookup in `LuminaSpecifier.kt` mirrors `FindSpecifier` and `SuggestSpecifier` from
`ReflectionSpecifiers.cpp`, including the edit-distance thresholds and the warning text.

## Publishing to the JetBrains Marketplace

1. Sign in at <https://plugins.jetbrains.com> with the JetBrains account that should own the plugin.
2. Upload `build/distributions/lumina-rider-<version>.zip` once by hand, under **Upload plugin**. The first
   upload creates the listing and reserves the plugin ID `dev.lumina.engine`. Pick the Rider product and a
   license, and expect a manual review on the first submission.
3. For later releases, generate a permanent token under **My Account | Personal Access Tokens** with the
   Marketplace scope, then set these environment variables and run `./gradlew publishPlugin`:

   - `PUBLISH_TOKEN`: the marketplace token
   - `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, `PRIVATE_KEY_PASSWORD`: the plugin signing key, per
     <https://plugins.jetbrains.com/docs/intellij/plugin-signing.html>

   Signing is optional for a first upload but required for updates once a key is registered, so generating one
   before the first release is worth doing.

4. Bump `pluginVersion` in `gradle.properties` and add a `<change-notes>` entry in `plugin.xml` for each
   release. `pluginSinceBuild` / `pluginUntilBuild` set the Rider versions the release is offered to.

## Compatibility

Built against Rider 2026.1 and offered to builds 261 through 262.*.
