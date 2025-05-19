import net.legacylauncher.gradle.*
import org.gradle.jvm.tasks.Jar

plugins {
    `java-library`
    `jvm-test-suite`
    `auto-version`
    alias(libs.plugins.lombok)
    net.legacylauncher.brand
}

val java11: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
    compileClasspath += sourceSets.main.get().output

    java {
        setSrcDirs(files("src/main/java11"))
    }
}

val compileJava11Java by tasks.getting(JavaCompile::class) {
    options.release = 11
}

java {
    registerFeature("java11") {
        usingSourceSet(java11)
    }
}

dependencies {
    implementation(projects.utils)

    api(libs.slf4j.api)
    api(libs.bundles.jna)
    api(libs.commons.lang3)

    "java11Implementation"(libs.system.theme.detector)
    "java11Implementation"(libs.bundles.dbus)
    "java11Implementation"(libs.junixsocket.core)
    "java11Implementation"(projects.dbusJavaTransportJunixsocket)

    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}

val jar by tasks.getting(Jar::class) {
    into("META-INF/versions/11") {
        from(java11.output)
    }

    manifest.attributes(
        "Multi-Release" to true
    )
}

@Suppress("UnstableApiUsage")
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

val launcherLibraries by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = true
    isCanBeDeclared = false

    extendsFrom(configurations.runtimeClasspath.get(), configurations["java11RuntimeClasspath"])

    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(LegacyLauncherPackaging.ATTRIBUTE, objects.named(LegacyLauncherPackaging.LAUNCHER_LIBRARY))
    }

    // dirty hack to get around gradle shittines in dependency resolution
    exclude("net.java.dev.jna", "jna")
}

artifacts {
    add(launcherLibraries.name, jar)
}
