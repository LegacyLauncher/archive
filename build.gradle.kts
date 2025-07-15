import org.openjfx.gradle.*

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(libs.jackson.module.kotlin)
        classpath(libs.jackson.dataformat.yaml)
    }
}

plugins {
    alias(libs.plugins.javafx) apply false
}

subprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
        maven("https://llaun.ch/repo/libraries") {
            name = "legacy launcher cdn"
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

    plugins.withId("java-base") {
        val targetJavaCompatibility: String by ext
        val sourceJavaCompatibility: String by ext

        extensions.configure<JavaPluginExtension>("java") {
            sourceCompatibility = JavaVersion.toVersion(sourceJavaCompatibility.toInt())
            targetCompatibility = JavaVersion.toVersion(targetJavaCompatibility.toInt())
            toolchain {
                val buildUsingJava: String by ext
                languageVersion = JavaLanguageVersion.of(buildUsingJava)
            }
        }

        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.release = targetJavaCompatibility.toInt()
        }
    }

    tasks.withType<AbstractArchiveTask>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
        duplicatesStrategy = DuplicatesStrategy.FAIL
    }

    plugins.withId("org.openjfx.javafxplugin") {
        extensions.configure<JavaFXOptions>("javafx") {
            version = "21.0.7"
            configuration = when (project.name) {
                "launcher" -> "implementation"
                else -> "runtimeOnly"
            }
            modules("javafx.base", "javafx.graphics", "javafx.media", "javafx.web", "javafx.swing")
        }
    }
}
