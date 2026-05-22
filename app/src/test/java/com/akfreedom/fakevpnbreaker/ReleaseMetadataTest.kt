package com.akfreedom.fakevpnbreaker

import java.nio.file.Path
import kotlin.io.path.readText
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseMetadataTest {
    private val buildGradle = Path.of("build.gradle.kts").readText()
    private val strings = Path.of("src", "main", "res", "values", "strings.xml").readText()

    @Test
    fun releaseMetadataTargetsVersionOneOneZero() {
        assertTrue(buildGradle.contains("""versionCode = 4"""))
        assertTrue(buildGradle.contains("""versionName = "1.1.0""""))
    }

    @Test
    fun versionDisplayStringUsesPackageVersionArgument() {
        assertTrue(strings.contains("""<string name="version_label">v%1${'$'}s</string>"""))
    }
}
