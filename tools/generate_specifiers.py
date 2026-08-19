"""Regenerates LuminaSpecifiers.kt from the engine's ReflectionSpecifiers.h.

Usage: python tools/generate_specifiers.py <path-to-LuminaEngine>
"""

import os
import re
import sys

BLOCKS = [
    ("LUMINA_REFLECT_SPECIFIERS", "REFLECT", False),
    ("LUMINA_PROPERTY_SPECIFIERS", "PROPERTY", False),
    ("LUMINA_FUNCTION_SPECIFIERS", "FUNCTION", False),
    ("LUMINA_SCRIPT_EXPORT_SPECIFIERS", "SCRIPT_EXPORT", False),
    ("LUMINA_RUNTIME_INJECTED_METADATA", "INJECTED", True),
]

ROW = re.compile(r'X\(\s*(\w+)\s*,\s*(\w+)\s*,\s*(\w+)\s*,\s*"((?:[^"\\]|\\.)*)"\s*\)')

HEADER_RELATIVE = os.path.join(
    "Engine", "Applications", "Reflector", "Source", "Reflector", "ReflectionSpecifiers.h"
)

OUTPUT_RELATIVE = os.path.join(
    "src", "main", "kotlin", "dev", "lumina", "rider", "specifiers", "LuminaSpecifiers.kt"
)


def read_block(text, macro):
    start = text.index("#define " + macro + "(X)")
    end = start
    while True:
        line_end = text.index("\n", end)
        line = text[end:line_end]
        end = line_end + 1
        if not line.rstrip().endswith("\\"):
            break
    return text[start:end]


def kotlin_string(value):
    return value.replace("\\", "\\\\").replace('"', '\\"').replace("$", "\\$")


def main():
    if len(sys.argv) < 2:
        print("usage: generate_specifiers.py <path-to-LuminaEngine>", file=sys.stderr)
        return 1

    engine_root = sys.argv[1]
    header_path = os.path.join(engine_root, HEADER_RELATIVE)
    with open(header_path, encoding="utf-8") as handle:
        text = handle.read()

    sections = []
    for macro, target, injected in BLOCKS:
        rows = ROW.findall(read_block(text, macro))
        if not rows:
            raise SystemExit("no rows parsed from " + macro)
        sections.append((target, injected, rows))

    lines = [
        "package dev.lumina.rider.specifiers",
        "",
        "// Generated from Engine/Applications/Reflector/Source/Reflector/ReflectionSpecifiers.h",
        "// by tools/generate_specifiers.py. Do not edit by hand.",
        "",
        "object LuminaSpecifierTable {",
        "",
    ]

    for target, injected, rows in sections:
        field = "INJECTED" if injected else target
        lines.append("    val %s: List<LuminaSpecifier> = listOf(" % field)
        for name, form, consumer, doc in rows:
            lines.append(
                '        LuminaSpecifier("%s", LuminaSpecifierForm.%s, LuminaSpecifierConsumer.%s, "%s"),'
                % (name, form.upper(), consumer.upper(), kotlin_string(doc))
            )
        lines.append("    )")
        lines.append("")

    lines.append("}")

    output_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))), OUTPUT_RELATIVE)
    os.makedirs(os.path.dirname(output_path), exist_ok=True)
    with open(output_path, "w", encoding="utf-8", newline="\n") as handle:
        handle.write("\n".join(lines) + "\n")

    total = sum(len(rows) for _, _, rows in sections)
    print("wrote %s (%d specifiers)" % (output_path, total))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
