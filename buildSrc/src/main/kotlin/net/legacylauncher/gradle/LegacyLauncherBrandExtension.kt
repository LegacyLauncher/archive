package net.legacylauncher.gradle

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

interface LegacyLauncherBrandExtension {
    val brand: Property<String>
    val displayName: Property<String>
    val version: Property<String>

    val supportEmail: Property<String>
}
