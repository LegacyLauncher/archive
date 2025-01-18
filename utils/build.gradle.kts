import net.legacylauncher.gradle.*

plugins {
    `java-library`
    `jvm-test-suite`
    `auto-version`
}

dependencies {
    api(libs.slf4j.api)
    api(libs.commons.lang3)
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

    extendsFrom(configurations.runtimeClasspath.get())

    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(LegacyLauncherPackaging.ATTRIBUTE, objects.named(LegacyLauncherPackaging.LAUNCHER_LIBRARY))
    }
}

artifacts {
    add(launcherLibraries.name, tasks.jar)
}
