package dev.lumina.rider.engine

import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals

class LuminaTargetsTest {

    private fun projectWith(vararg targets: String): Path {
        val root = createTempDirectory("lumina-project")
        val source = root.resolve("Source").createDirectories()
        targets.forEach { Files.writeString(source.resolve("$it.Target.cs"), "// $it") }
        return root
    }

    @Test
    fun `discovery finds every target under the project source`() {
        val root = projectWith("MyGame", "MyGameServer")
        assertEquals(listOf("MyGame", "MyGameServer"), LuminaTargets.discover(null, root))
    }

    @Test
    fun `the target named after the project wins`() {
        val root = projectWith("MyGame", "MyGameServer")
        assertEquals("MyGame", LuminaEngine.derivedTarget(root, "MyGame"))
    }

    @Test
    fun `a lone target is used even when the name does not match`() {
        val root = projectWith("Gameplay")
        assertEquals("Gameplay", LuminaEngine.derivedTarget(root, "SomethingElse"))
    }

    @Test
    fun `an ambiguous project falls back to the engine editor`() {
        val root = projectWith("Alpha", "Beta")
        assertEquals(LuminaEngine.DEFAULT_TARGET, LuminaEngine.derivedTarget(root, "SomethingElse"))
    }

    @Test
    fun `no project means the engine editor`() {
        assertEquals(LuminaEngine.DEFAULT_TARGET, LuminaEngine.derivedTarget(null, null))
        assertEquals(LuminaEngine.DEFAULT_TARGET, LuminaEngine.derivedTarget(createTempDirectory("empty"), "Whatever"))
    }
}
