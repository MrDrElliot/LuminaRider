package dev.lumina.rider

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

object LuminaIcons {

    @JvmField
    val Lumina: Icon = load("/icons/lumina.svg")

    @JvmField
    val Build: Icon = load("/icons/build.svg")

    @JvmField
    val Reflect: Icon = load("/icons/reflect.svg")

    @JvmField
    val Generate: Icon = load("/icons/generate.svg")

    private fun load(path: String): Icon = IconLoader.getIcon(path, LuminaIcons::class.java)
}
