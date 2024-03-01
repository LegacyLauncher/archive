@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    embeddedKotlin("jvm")
}

java {
    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 21
}

kotlin.target {
    compilations.configureEach {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_21
        }
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(gradleKotlinDsl())
    implementation(embeddedKotlin("stdlib"))
}

gradlePlugin {
    plugins {
        register("brand") {
            id = "net.legacylauncher.brand"
            implementationClass = "net.legacylauncher.gradle.LegacyLauncherBrandPlugin"
        }
    }
}