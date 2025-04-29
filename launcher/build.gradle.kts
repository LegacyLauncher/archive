@file:Suppress("UnstableApiUsage")

import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.dataformat.yaml.*
import com.fasterxml.jackson.module.kotlin.*
import net.legacylauncher.gradle.*
import net.legacylauncher.gradle.LegacyLauncherS3UploadTask.Companion.bucketProperty
import org.gradle.jvm.tasks.Jar
import java.security.*

plugins {
    java
    `jvm-test-suite`
    alias(libs.plugins.javafx)
    alias(libs.plugins.buildconfig)
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

val compileTestJava by tasks.getting(JavaCompile::class) {
    options.release = 11
}

val launcherLibraries by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = true
    isCanBeDeclared = true

    extendsFrom(configurations.runtimeClasspath.get(), configurations["java11RuntimeClasspath"])
    // TODO this line removes JavaFX from debug runs
    exclude(group = "org.openjfx")
    // we don't need an empty jar
    exclude(group = "com.google.guava", module = "listenablefuture")

    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(LegacyLauncherPackaging.ATTRIBUTE, objects.named(LegacyLauncherPackaging.LAUNCHER_LIBRARY))
    }
}

evaluationDependsOn(projects.common.path)

dependencies {
    compileOnly(projects.bridge) // bootstrap-java will inject it, all others don't need and don't use it

    annotationProcessor(libs.log4j.core)

    implementation(projects.utils)
    implementation(projects.common)
    implementation(libs.authlib) {
        exclude(group = "com.google.guava", module = "guava")
    }
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
    implementation(libs.miglayout)
    implementation(libs.nanohttpd)
    implementation(libs.nstweaker)
    implementation(libs.oshi)
    implementation(libs.toml4j)
    implementation(libs.xz)

    "java11CompileOnly"(projects.common) {
        capabilities {
            requireFeature("java11")
        }
    }
    "java11Implementation"(libs.bundles.dbus)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.mockito.core)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.mockito.junit.jupiter)
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

val processResources by tasks.getting(ProcessResources::class) {
    inputs.property("productVersion", brand.version.get())
    inputs.property("shortBrand", brand.brand.get())
    inputs.property("fullBrand", brand.displayName.get())
    inputs.files(launcherLibraries)

    doLast {
        val meta = mapOf(
            "version" to brand.version.get(),
            "shortBrand" to brand.brand.get(),
            "brand" to brand.displayName.get(),
            "libraries" to launcherLibraries.resolvedConfiguration.resolvedArtifacts.map { artifact ->
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
                "bridgedEntryPoint" to "net.legacylauncher.LegacyLauncherBridged", // deprecated
                "entryPoint" to "net.legacylauncher.LegacyLauncherEntrypoint", // deprecated
                "entrypoints" to mapOf(
                    "bridge" to mapOf(
                        "type" to "net.legacylauncher.LegacyLauncherBridged",
                        "method" to "launch"
                    ),
                    "dbusP2P" to mapOf(
                        "type" to "net.legacylauncher.LegacyLauncherEntrypoint",
                        "method" to "launchP2P"
                    ),
                    "dbusSession" to mapOf(
                        "type" to "net.legacylauncher.LegacyLauncherEntrypoint",
                        "method" to "launchSession"
                    ),
                ),
                "repositories" to (brand.repoHosts.get()
                    .map { "https://$it" } + brand.repoCdnPathPrefixes.get()).map { "$it/repo/libraries" },
                "javaVersion" to "[11,)", // recommended java version as per https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html
            )
        )
    }
}

val generateUpdateJson by tasks.registering {
    dependsOn(jar)

    inputs.property("productVersion", brand.version.get())
    inputs.property("repoHosts", brand.repoHosts.get())
    inputs.file("changelog.yml")
    val updateJsonFile = layout.buildDirectory.file("update/${brand.brand.get()}/launcher.json")
    outputs.file(updateJsonFile)

    doLast {
        val jarFileChecksum = generateChecksum(jar.outputs.files.singleFile)

        val changelog = when (System.getenv("INCLUDE_CHANGELOG")) {
            "true" -> file("changelog.yml").reader().use { reader ->
                YAMLMapper().readValue<Map<String, String>>(reader)
            }

            else -> null
        }

        val meta = mapOf(
            "version" to brand.version.get(),
            "checksum" to jarFileChecksum,
            "description" to changelog,
        )

        updateJsonFile.get().asFile.writer().use { writer ->
            ObjectMapper().writeValue(writer, meta)
        }
    }
}

val copyJarAndRename by tasks.registering {
    dependsOn(jar)
    val targetFile = layout.buildDirectory.file("update/${brand.brand.get()}/launcher.jar")
    outputs.file(targetFile)
    doLast {
        jar.outputs.files.singleFile.copyTo(targetFile.get().asFile, overwrite = true)
    }
}

val generateSha256File by tasks.registering {
    dependsOn(jar)
    val file = layout.buildDirectory.file("update/${brand.brand.get()}/launcher.jar.sha256")
    outputs.file(file)
    doLast {
        file.get().asFile.writeText(generateChecksum(jar.outputs.files.singleFile))
    }
}

buildConfig {
    className("BuildConfig")
    packageName("net.legacylauncher.configuration")

    useJavaOutput()

    buildConfigField("String", "SHORT_BRAND", "\"${brand.brand.get()}\"")
    buildConfigField("String", "FULL_BRAND", "\"${brand.displayName.get()}\"")
    buildConfigField("String", "VERSION", "\"${brand.version.get()}\"")
    buildConfigField("String", "SUPPORT_EMAIL", "\"${brand.supportEmail.get()}\"")
}

val prepareDeploy by tasks.registering {
    dependsOn(generateUpdateJson, copyJarAndRename, generateSha256File)
    outputs.files(dependsOn)
}

val deployLauncherBrand by tasks.registering(LegacyLauncherS3UploadTask::class) {
    bucket = project.bucketProperty(LegacyLauncherS3UploadTask.Companion.Buckets.BRANDS)
    entityPrefix = "launcher"
    inputs.files(prepareDeploy)
}

val deployLauncherUpdate by tasks.registering(LegacyLauncherS3UploadTask::class) {
    dependsOn(prepareDeploy)
    bucket = project.bucketProperty(LegacyLauncherS3UploadTask.Companion.Buckets.UPDATES)
    entityPrefix = "launcher"
    fileName = generateSha256File.map { it.outputs.files.singleFile.readText() + ".jar" }
    inputs.files(copyJarAndRename)
}

val deploy by tasks.registering {
    group = "deploy"
    dependsOn(deployLauncherBrand, deployLauncherUpdate)
}

val assemble: Task by tasks.getting {
    dependsOn(prepareDeploy)
}

val jar by tasks.getting(Jar::class) {
    into("META-INF/versions/11") {
        from(java11.output)
    }

    manifest.attributes(
        "Multi-Release" to true
    )
}

val test by tasks.getting(Test::class) {
    dependsOn(processResources)
    systemProperty("net.legacylauncher.test.launcher-resources", processResources.destinationDir)
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

val launcherJar by configurations.consumable("launcherJar") {
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(LegacyLauncherPackaging.ATTRIBUTE, objects.named(LegacyLauncherPackaging.LAUNCHER_JAR))
    }
}

artifacts {
    add(launcherJar.name, jar)
}
