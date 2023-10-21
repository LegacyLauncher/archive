@file:Suppress("UnstableApiUsage")

import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.jvm.tasks.Jar
import java.security.MessageDigest


plugins {
    java
    `jvm-test-suite`
    id("org.openjfx.javafxplugin")
    id("com.github.gmazzo.buildconfig")
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
    options.release.set(11)
}

val compileTestJava by tasks.getting(JavaCompile::class) {
    options.release.set(11)
}

val exportedClasspath by configurations.resolvable("exportedClasspath") {
    extendsFrom(configurations.runtimeClasspath.get(), configurations["java11RuntimeClasspath"])
    // TODO this line removes JavaFX from debug runs
    exclude(group = "org.openjfx")
}

evaluationDependsOn(projects.common.identityPath.path)

dependencies {
    compileOnly(projects.bridge) // bootstrap-java will inject it, all others don't need and don't use it

    annotationProcessor(libs.log4j.core)

    implementation(projects.common)
    implementation(libs.authlib)
    implementation(libs.bundles.httpcomponents)
    implementation(libs.bundles.log4j)
    implementation(libs.commons.compress)
    implementation(libs.commons.io)
    implementation(libs.flatlaf)
    implementation(libs.gson)
    implementation(libs.guava)
    implementation(libs.jackson.core)
    implementation(libs.jarscanner)
    implementation(libs.java.semver)
    implementation(libs.java.statsd.client)
    implementation(libs.jdom)
    implementation(libs.jopt.simple)
    implementation(libs.jvd)
    implementation(libs.nanohttpd)
    implementation(libs.nstweaker)
    implementation(libs.oshi)
    implementation(libs.sentry)
    implementation(libs.toml4j)
    implementation(libs.xz)

    "java11CompileOnly"(projects.common.dependencyProject.sourceSets["java11"].output)
    "java11Implementation"(libs.bundles.dbus)
    "java11RuntimeOnly"(projects.dbusJavaTransportJunixsocket)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.mockito.core)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.mockito.junit.jupiter)
    testRuntimeOnly(libs.mockito.inline)
}

fun resolveLauncherClasspath(): Collection<ResolvedArtifact> {
    return exportedClasspath.resolvedConfiguration.resolvedArtifacts.filter { artifact ->
        artifact.extension == "jar"
    }
}

val launcherClasspath: Provider<Collection<ResolvedArtifact>> by ext.invoke { providers.provider(::resolveLauncherClasspath) }

val shortBrand: String by rootProject.ext
val fullBrand: String by rootProject.ext
val productVersion: String by ext

val buildLauncherRepo by tasks.registering(Sync::class) {
    destinationDir = rootDir.resolve("lib/${shortBrand}")
    inputs.files(exportedClasspath)
    resolveLauncherClasspath().forEach { artifact ->
        val path = with(artifact.moduleVersion.id) {
            "${group.replace('.', '/')}/$name/$version"
        }
        into(path) {
            from(artifact.file)
        }
    }
}

fun ByteArray.encodeHex(): String = buildString(size * 2) {
    this@encodeHex.forEach { b ->
        append(b.toUByte().toString(16).padStart(2, '0'))
    }
}

fun generateChecksum(file: File, algorithm: String = "SHA-256"): String = file.inputStream().use { inputStream ->
    val digest = MessageDigest.getInstance(algorithm)
    val buffer = ByteArray(8192)
    var read: Int
    while (inputStream.read(buffer).also { read = it } >= 0) {
        digest.update(buffer, 0, read)
    }
    digest.digest().encodeHex()
}

fun formatShortArtifactNotation(artifact: ResolvedArtifact): String {
    val name = artifact.moduleVersion.toString()
    artifact.classifier?.let { classifier ->
        return "$name:${classifier}"
    }
    return name
}

fun writeMeta(file: File, content: Map<String, Any>) {
    file.parentFile.mkdirs()
    file.writer().use { writer ->
        ObjectMapper().writeValue(writer, content)
    }
}

val repoHosts: Collection<String> by rootProject.ext
val repoCdnPathPrefixes: Collection<String> by rootProject.ext

val processResources by tasks.getting(ProcessResources::class) {
    inputs.files(exportedClasspath)
    inputs.property("productVersion", productVersion)
    inputs.property("shortBrand", shortBrand)
    inputs.property("fullBrand", fullBrand)

    doLast {
        val meta = mapOf(
            "version" to productVersion,
            "shortBrand" to shortBrand,
            "brand" to fullBrand,
            "libraries" to resolveLauncherClasspath().map { artifact ->
                mapOf(
                    "name" to formatShortArtifactNotation(artifact),
                    "checksum" to generateChecksum(artifact.file),
                )
            },
        )

        // meta for old bootstrap-java
        writeMeta(
            destinationDir.resolve("ru/turikhay/tlauncher/meta.json"),
            meta + mapOf(
                "mainClass" to "net.legacylauncher.LegacyLauncherBridged",
            )
        )

        // meta for *modern* bootstraps :)
        writeMeta(
            destinationDir.resolve("META-INF/launcher-meta.json"),
            meta + mapOf(
                "bridgedEntryPoint" to "net.legacylauncher.LegacyLauncherBridged",
                "entryPoint" to "net.legacylauncher.LegacyLauncherEntrypoint",
                "repositories" to (repoHosts.map { "https://$it" } + repoCdnPathPrefixes).map { "$it/repo/libraries" },
                "javaVersion" to "[11,)", // recommended java version as per https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html
            )
        )
    }
}

object UrlComparator : Comparator<String> {
    override fun compare(o1: String, o2: String): Int = when {
        o1.startsWith("https") -> when {
            o2.startsWith("https") -> 0
            else -> -1
        }

        o2.startsWith("https") -> 1
        else -> 0
    }
}

val generateUpdateJson by tasks.registering {
    dependsOn(jar)

    inputs.property("productVersion", productVersion)
    inputs.property("repoHosts", repoHosts)
    inputs.file("changelog.yml")
    val updateJsonFile = layout.buildDirectory.file("update/${shortBrand}/launcher.json")
    outputs.file(updateJsonFile)

    doLast {
        val jarFileChecksum = generateChecksum(jar.outputs.files.singleFile)
        val downloadPath = "repo/update/${shortBrand}/launcher/${jarFileChecksum}.jar"
        val downloadUrlList = repoHosts.flatMap { host ->
            listOf("https", "http").map { scheme ->
                "$scheme://$host/$downloadPath"
            }
        }.sortedWith(UrlComparator)

        val changelog = when (System.getenv("INCLUDE_CHANGELOG")) {
            "true" -> file("changelog.yml").reader().use { reader ->
                ObjectMapper().readTree(reader)
            }

            else -> null
        }

        val meta = mapOf(
            "version" to productVersion,
            "checksum" to jarFileChecksum,
            "url" to downloadUrlList,
            "description" to changelog,
        )

        updateJsonFile.get().asFile.writer().use { writer ->
            ObjectMapper().writeValue(writer, meta)
        }
    }
}

val copyJarAndRename by tasks.registering(Copy::class) {
    from(jar)
    into(layout.buildDirectory.dir("update/$shortBrand"))
    rename { "launcher.jar" }
}

val generateSha256File by tasks.registering {
    dependsOn(jar)
    val file = layout.buildDirectory.file("update/${shortBrand}/launcher.jar.sha256")
    outputs.file(file)
    doLast {
        file.get().asFile.writeText(generateChecksum(jar.outputs.files.singleFile))
    }
}

buildConfig {
    className("BuildConfig")
    packageName("net.legacylauncher.configuration")

    useJavaOutput()

    buildConfigField("String", "SHORT_BRAND", "\"${shortBrand}\"")
    buildConfigField("String", "FULL_BRAND", "\"${fullBrand}\"")
    buildConfigField("String", "VERSION", "\"${productVersion}\"")
}

val assemble: Task by tasks.getting {
    dependsOn(generateUpdateJson, copyJarAndRename, generateSha256File)
}

val jar by tasks.getting(Jar::class) {
    into("META-INF/versions/11") {
        from(java11.output)
    }

    manifest.attributes(
        "Multi-Release" to true
    )
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}