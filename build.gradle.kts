import org.openjfx.gradle.JavaFXOptions

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")
    }
}

plugins {
    id("org.openjfx.javafxplugin") version "0.1.0" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("com.github.gmazzo.buildconfig") version "4.1.2" apply false
    id("de.undercouch.download") version "5.5.0" apply false
}

val shortBrand: String by ext.invoke { System.getenv("SHORT_BRAND") ?: "develop" }
val fullBrand: String by ext.invoke {
    when (shortBrand) {
        "develop" -> "Dev"
        "legacy" -> "Stable"
        "legacy_beta" -> "Beta"
        "mcl" -> "for Mc-launcher.com"
        "aur" -> "AUR"
        "appt" -> "для AppStorrent"
        else -> shortBrand
    }
}
val repoDomains: Collection<String> by ext.invoke {
    listOf("llaun.ch", "lln4.ru")
}
val repoDomainsZonesEu: Collection<String> by ext.invoke {
    listOf("eu1", "eu2", "eu3")
}
val repoDomainsZonesRu: Collection<String> by ext.invoke {
    listOf("ru1", "ru2", "ru3")
}
val repoHosts: Collection<String> by ext.invoke {
    repoDomains.flatMap { domain ->
        (repoDomainsZonesEu + repoDomainsZonesRu).map { zone ->
            "$zone.$domain"
        }
    }
}
val repoCdnPathPrefixes: Collection<String> by ext.invoke {
    listOf("https://cdn.turikhay.ru/lln4")
}

subprojects {
    val productVersion: String by ext.invoke {
        "${project.version}+${
            shortBrand.replace(
                Regex("[^\\dA-Za-z\\-]"),
                "-"
            )
        }${System.getenv("VERSION_SUFFIX") ?: ""}"
    }

    plugins.withId("java-base") {
        repositories {
            mavenCentral()
            maven("https://jitpack.io")
            maven("https://cdn.turikhay.ru/tlauncher/repo/libraries") {
                name = "turikhay cdn"
                metadataSources {
                    mavenPom()
                    artifact()
                }
                mavenContent {
                    includeGroup("me.cortex")
                    includeGroup("ru.turikhay")
                    includeGroup("ru.turikhay.app")
                }
            }
            maven("https://libraries.minecraft.net") {
                name = "Minecraft"
                mavenContent {
                    includeGroup("com.mojang")
                }
            }
            maven("https://oss.sonatype.org/content/repositories/snapshots") {
                mavenContent {
                    snapshotsOnly()
                }
            }
        }

        val targetJavaCompatibility: String by ext
        val sourceJavaCompatibility: String by ext

        extensions.configure<JavaPluginExtension>("java") {
            sourceCompatibility = JavaVersion.toVersion(sourceJavaCompatibility.toInt())
            targetCompatibility = JavaVersion.toVersion(targetJavaCompatibility.toInt())
            toolchain {
                val buildUsingJava: String by ext
                languageVersion.set(JavaLanguageVersion.of(buildUsingJava))
            }
        }

        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.release.set(targetJavaCompatibility.toInt())
        }
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
        duplicatesStrategy = DuplicatesStrategy.FAIL
    }

    plugins.withId("org.openjfx.javafxplugin") {
        extensions.configure<JavaFXOptions>("javafx") {
            version = "11.0.2"
            configuration = when (project.name) {
                "launcher" -> "implementation"
                else -> "runtimeOnly"
            }
            modules("javafx.base", "javafx.graphics", "javafx.media", "javafx.web", "javafx.swing")
        }
    }
}
