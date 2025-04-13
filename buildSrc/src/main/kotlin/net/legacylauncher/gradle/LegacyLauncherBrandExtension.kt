package net.legacylauncher.gradle

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

interface LegacyLauncherBrandExtension {
    val brand: Property<String>
    val displayName: Property<String>
    val version: Property<String>

    val repoDomains: ListProperty<String>
    val repoDomainsZonesEu: ListProperty<String>
    val repoDomainsZonesRu: ListProperty<String>
    val repoHosts: ListProperty<String>
    val repoCdnPathPrefixes: ListProperty<String>
    val updateRepoPrefixes: ListProperty<String>

    val supportEmail: Property<String>
}
