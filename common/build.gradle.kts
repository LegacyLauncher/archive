import org.gradle.jvm.tasks.Jar

plugins {
    `java-library`
    `jvm-test-suite`
}

version = projects.launcher.version!!

val java11: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
    compileClasspath += sourceSets.main.get().output

    java {
        setSrcDirs(files("src/main/java11"))
    }
}

val compileJava11Java by tasks.getting(JavaCompile::class) {
    options.release.set(11)
}

dependencies {
    api(libs.slf4j.api)
    api(libs.bundles.jna)
    api(libs.commons.lang3)

    implementation(libs.system.theme.detector)
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

val repoDomains: Collection<String> by rootProject.ext
val repoDomainsZonesEu: Collection<String> by rootProject.ext
val repoDomainsZonesRu: Collection<String> by rootProject.ext
val repoCdnPathPrefixes: Collection<String> by rootProject.ext

val processResources by tasks.getting(ProcessResources::class) {
    val repoProperties = mapOf(
        "domains" to repoDomains.joinToString(","),
        "eu_prefixes" to repoDomainsZonesEu.joinToString(","),
        "ru_prefixes" to repoDomainsZonesRu.joinToString(","),
        "cdn_prefixes" to repoCdnPathPrefixes.joinToString(","),
    )

    inputs.property("repoProperties", repoProperties)

    doLast {
        val file = destinationDir.resolve("net/legacylauncher/repository/repositories_v1.properties")
        file.writer().use { writer ->
            repoProperties.forEach { (key, value) ->
                writer.append("$key=$value\n")
            }
        }
    }
}
