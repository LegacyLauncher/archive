package net.legacylauncher.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create

class LegacyLauncherBrandPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create<LegacyLauncherBrandExtension>("brand")

        extension.brand.convention(System.getenv("SHORT_BRAND") ?: "develop")
        extension.displayName.convention(extension.brand.map { brand ->
            when (brand) {
                "develop" -> "Dev"
                "legacy" -> "Stable"
                "legacy_beta" -> "Beta"
                "mcl" -> "for Mc-launcher.com"
                "aur" -> "AUR"
                "appt" -> "для AppStorrent"
                else -> brand
            }
        })
        extension.version.convention(extension.brand.map { brand ->
            "${project.version}+${brand.replace(Regex("[^\\dA-Za-z\\-]"), "-")}${System.getenv("VERSION_SUFFIX") ?: ""}"
        })

        extension.repoDomains.convention(listOf("llaun.ch", "lln4.ru"))
        extension.repoDomainsZonesEu.convention(listOf("eu1", "eu2"))
        extension.repoDomainsZonesRu.convention(listOf("ru1", "ru2", "ru3"))

        extension.repoHosts.convention(extension.repoDomains.map { repoDomains ->
            repoDomains.flatMap { domain ->
                (extension.repoDomainsZonesEu.get() + extension.repoDomainsZonesRu.get()).map { zone ->
                    "$zone.$domain"
                }
            }
        })
        extension.repoCdnPathPrefixes.convention(listOf("https://cdn.turikhay.ru/lln4"))
    }
}