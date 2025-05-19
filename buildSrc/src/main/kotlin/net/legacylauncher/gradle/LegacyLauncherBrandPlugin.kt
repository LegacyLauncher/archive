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

        extension.supportEmail.convention("support@llaun.ch")
    }
}
