import org.gradle.jvm.tasks.Jar

plugins {
    `java-library`
    `jvm-test-suite`
    `auto-version`
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

dependencies {
    implementation(projects.utils)

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

val processResources by tasks.getting(ProcessResources::class) {
    val repoProperties = mapOf(
        "domains" to brand.repoDomains.get().joinToString(","),
        "eu_prefixes" to brand.repoDomainsZonesEu.get().joinToString(","),
        "ru_prefixes" to brand.repoDomainsZonesRu.get().joinToString(","),
        "cdn_prefixes" to brand.repoCdnPathPrefixes.get().joinToString(","),
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
