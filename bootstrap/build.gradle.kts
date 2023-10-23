import com.fasterxml.jackson.databind.ObjectMapper
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import java.nio.charset.Charset
import java.security.MessageDigest

plugins {
    java
    `jvm-test-suite`
    id("com.github.johnrengelman.shadow")
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

val dev: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
    compileClasspath += sourceSets.main.get().output
}

val compileJava11Java by tasks.getting(JavaCompile::class) {
    options.release.set(11)
}


val compileTestJava by tasks.getting(JavaCompile::class) {
    options.release.set(11)
}

val compileDevJava by tasks.getting(JavaCompile::class) {
    options.release.set(11)
}

evaluationDependsOn(projects.common.identityPath.path)

dependencies {
    implementation(projects.bridge)
    implementation(projects.common)
    implementation(libs.commons.compress)
    implementation(libs.commons.io)
    implementation(libs.flatlaf)
    implementation(libs.gson)
    implementation(libs.java.semver)
    implementation(libs.java.statsd.client)
    implementation(libs.jopt.simple)
    implementation(libs.oshi)
    implementation(libs.raven)
    implementation(libs.slf4j.api)
    implementation(libs.slf4j.simple)

    "java11CompileOnly"(projects.common.dependencyProject.sourceSets["java11"].output)
    "java11Implementation"(libs.bundles.dbus)
    "java11Implementation"(libs.junixsocket.core)
    "java11Implementation"(projects.dbusJavaTransportJunixsocket)

    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.mockito.core)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.mockito.junit.jupiter)
    testRuntimeOnly(libs.mockito.inline)
}

val shadowJar by tasks.getting(ShadowJar::class) {
    configurations = listOf(
        project.configurations.runtimeClasspath.get(),
        project.configurations[java11.runtimeClasspathConfigurationName],
    )

    relocate("com.", "shaded.com.") {
        exclude("com.sun.**")
        exclude("/com/sun/**")
        exclude("/com/apple/laf/**")
        exclude("com.formdev.**")
        exclude("com.rm5248.dbusjava.**")
        exclude("/com/rm5248/dbusjava/**")
        exclude("com.kenai.**")
        exclude("/com/kenai/**")
        exclude("com.feralinteractive.**")
        exclude("/com/feralinteractive/**")
        exclude("com.oshi.**")
    }
    relocate("de.", "shaded.de.")
    relocate("io.", "shaded.io.")
//    relocate("joptsimple.", "shaded.joptsimple.")
    relocate("org.", "shaded.org.") {
        exclude("org.freedesktop.**")
        exclude("/org/freedesktop/**")
        exclude("org.newsclub.**")
        exclude("org.slf4j.**")
    }
    relocate("net.legacylauncher.util.windows.wmi", "shaded.net.legacylauncher.util.windows.wmi")
    relocate("net.", "shaded.net.") {
        exclude("net.legacylauncher.**")
        exclude("/net/legacylauncher/**")
        exclude("net.hadess.**")
        exclude("/net/hadess/**")
    }

    exclude("*.md")
    exclude("*module-info.class")
    exclude("LICENSE")
    exclude("META-INF/LICENSE*")
    exclude("META-INF/NOTICE*")

    dependencies {
        exclude(dependency("org.openjfx:.*"))
    }

    into("META-INF/versions/11") {
        from(java11.output)
    }

    manifest.attributes(
        "Main-Class" to "net.legacylauncher.bootstrap.BootstrapStarter",
        "Multi-Release" to true,
    )
}

evaluationDependsOn(projects.launcher.identityPath.path)

val runDebug by tasks.registering(JavaExec::class) {
    group = "Execution"
    description = "Run BootstrapDebug"
    maxHeapSize = "256M"
    mainClass.set("net.legacylauncher.bootstrap.BootstrapDebug")

    System.getenv("JRE_EXECUTABLE")?.let {
        executable(it)
    }

    if (System.getenv("RUN_EXTERNAL") == "true") {
        jvmArgs("-Dtlauncher.bootstrap.debug.external=true")
    }

    System.getenv("UI_SCALE")?.let {
        jvmArgs("-Dsun.java2d.uiScale=$it")
    }

    jvmArgs("-Dtlauncher.logFolder=${rootDir}/logs")
    jvmArgs("-Dtlauncher.systemCharset=${Charset.defaultCharset().name()}")
    if (DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-Dapple.awt.application.appearance=system")
    }
    args("--debug")


    val librariesDir = projects.launcher.dependencyProject.tasks.named<Sync>("buildLauncherRepo").get()
    val launcherJar = projects.launcher.dependencyProject.tasks.named<Jar>("jar").get()
    dependsOn(librariesDir, launcherJar)
    environment("LL_LAUNCHER_JAR", launcherJar.archiveFile.get().asFile)
    environment("LL_LIBRARIES_DIR", librariesDir.destinationDir)

    if (System.getenv("JRE_LEGACY") == "true") {
        javaLauncher.set(javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(8))
        })
        classpath(dev.runtimeClasspath)
    } else {
        classpath(java11.runtimeClasspath, dev.runtimeClasspath)
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

val shortBrand: String by rootProject.ext
val fullBrand: String by rootProject.ext
val productVersion: String by ext

buildConfig {
    className("BuildConfig")
    packageName("net.legacylauncher.bootstrap")

    useJavaOutput()

    buildConfigField("String", "SHORT_BRAND", "\"${shortBrand}\"")
    buildConfigField("String", "FULL_BRAND", "\"${fullBrand}\"")
    buildConfigField("String", "VERSION", "\"${productVersion}\"")
}

val processResources by tasks.getting(ProcessResources::class) {
    val meta = mapOf(
        "version" to productVersion,
        "shortBrand" to shortBrand,
        "brand" to fullBrand,
    )

    inputs.property("meta", meta)

    doLast {
        setOf(
            "ru/turikhay/tlauncher/bootstrap/meta.json",
            "META-INF/bootstrap-meta.json",
        ).map {
            destinationDir.resolve(it)
        }.forEach { file ->
            file.parentFile.mkdirs()
            file.writer().use { writer ->
                ObjectMapper().writeValue(writer, meta)
            }
        }
    }
}

val repoHosts: Collection<String> by rootProject.ext
val repoDomains: Collection<String> by rootProject.ext

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
    dependsOn(shadowJar)
    inputs.property("productVersion", productVersion)
    inputs.property("repoHosts", repoHosts)
    val updateJsonFile = layout.buildDirectory.file("update/${shortBrand}/bootstrap.json")
    outputs.file(updateJsonFile)

    doLast {
        val jarFileChecksum = generateChecksum(shadowJar.outputs.files.singleFile)
        val downloadPath = "repo/update/${shortBrand}/bootstrap/${jarFileChecksum}.jar"
        val meta = mapOf(
            "version" to productVersion,
            "checksum" to jarFileChecksum,
            "url" to repoDomains.map { domain ->
                "https://$domain/$downloadPath"
            } + repoHosts.flatMap { host ->
                listOf("https", "http").map { scheme ->
                    "$scheme://$host/$downloadPath"
                }
            }.sortedWith(UrlComparator)
        )

        updateJsonFile.get().asFile.writer().use { writer ->
            ObjectMapper().writeValue(writer, meta)
        }
    }
}

val copyJarAndRename by tasks.registering(Copy::class) {
    from(shadowJar)
    into(layout.buildDirectory.dir("update/$shortBrand"))
    rename { "bootstrap.jar" }
}

val generateSha256File by tasks.registering {
    dependsOn(shadowJar)
    val file = layout.buildDirectory.file("update/${shortBrand}/bootstrap.jar.sha256")
    outputs.file(file)
    doLast {
        file.get().asFile.writeText(generateChecksum(shadowJar.outputs.files.singleFile))
    }
}

val assemble: Task by tasks.getting {
    dependsOn(generateUpdateJson, copyJarAndRename, generateSha256File)
}

@Suppress("UnstableApiUsage")
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}