package net.legacylauncher.gradle

import org.gradle.api.*
import org.gradle.api.attributes.*

interface LegacyLauncherPackaging : Named {
    companion object {
        @JvmStatic
        val ATTRIBUTE: Attribute<LegacyLauncherPackaging> = Attribute.of(
            "net.legacylauncher.packaging",
            LegacyLauncherPackaging::class.java
        )

        const val LAUNCHER_LIBRARY: String = "launcher-library"
        const val LAUNCHER_JAR: String = "launcher-jar"
        const val BOOTSTRAP_JAR: String = "bootstrap-jar"
    }
}
