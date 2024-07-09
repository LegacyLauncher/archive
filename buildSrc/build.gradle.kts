import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `embedded-kotlin`
}

java {
    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = JavaVersion.VERSION_21
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 21
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    implementation(gradleKotlinDsl())
}

gradlePlugin {
    plugins {
        register("brand") {
            id = "net.legacylauncher.brand"
            implementationClass = "net.legacylauncher.gradle.LegacyLauncherBrandPlugin"
        }
    }
}
