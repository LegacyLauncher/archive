import com.fasterxml.jackson.databind.ObjectMapper
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform
import org.springframework.boot.gradle.tasks.bundling.BootJar
import java.nio.charset.Charset
import java.security.MessageDigest

plugins {
    java
    `jvm-test-suite`
    alias(libs.plugins.javafx)
    alias(libs.plugins.buildconfig)
    alias(libs.plugins.lombok)
    alias(libs.plugins.spring.boot)
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

val dev: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().compileClasspath
    runtimeClasspath += sourceSets.main.get().runtimeClasspath
    compileClasspath += sourceSets.main.get().output
}

val boot: SourceSet by sourceSets.creating {}

val compileJava11Java by tasks.getting(JavaCompile::class) {
    options.release = 11
}

val compileTestJava by tasks.getting(JavaCompile::class) {
    options.release = 11
}

val compileDevJava by tasks.getting(JavaCompile::class) {
    if (System.getenv("JRE_LEGACY") == "true") {
        targetCompatibility = "8"
        sourceCompatibility = "8"
    } else {
        options.release = 11
    }
}

val compileBootJava by tasks.getting(JavaCompile::class) {
    targetCompatibility = "8"
    sourceCompatibility = "8"
}

evaluationDependsOn(projects.common.identityPath.path)

dependencies {
    implementation(projects.utils)
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

    "bootCompileOnly"(libs.spring.boot.loader)
}

val jar by tasks.getting(Jar::class) {
    into("META-INF/versions/11") {
        from(java11.output)
    }

    manifest.attributes(
        "Main-Class" to "net.legacylauncher.bootstrap.BootstrapStarter",
        "Multi-Release" to "true",
    )
}

val bootClasspath: Configuration by configurations.creating {
    extendsFrom(configurations.runtimeClasspath.get())
    extendsFrom(project.configurations[java11.runtimeClasspathConfigurationName])
    exclude(group = "org.openjfx")
}

val bootJar by tasks.getting(BootJar::class) {
    mainClass.set("net.legacylauncher.bootstrap.BootstrapStarter")
    classpath = files(bootClasspath, jar)
    from(boot.output)
}

val devJar by tasks.registering(Jar::class) {
    archiveClassifier.set("dev")
    from(sourceSets.main.map { it.output })
    from(dev.output)
    manifest.attributes(
        "Main-Class" to "net.legacylauncher.bootstrap.BootstrapStarter",
        "Multi-Release" to "true",
    )
}

evaluationDependsOn(projects.launcher.identityPath.path)

fun JavaExec.commonRun() {
    group = "Execution"
    maxHeapSize = "256M"

    System.getenv("JRE_EXECUTABLE")?.let {
        executable(it)
    }

    if (System.getenv("RUN_EXTERNAL") == "true") {
        jvmArgs("-Dtlauncher.bootstrap.debug.external=true")
    }

    System.getenv("UI_SCALE")?.let {
        jvmArgs("-Dsun.java2d.uiScale=$it")
    }


    jvmArgs("-Dtlauncher.logFolder=${layout.buildDirectory.dir("logs").get().asFile}")
    jvmArgs("-Dtlauncher.systemCharset=${Charset.defaultCharset().name()}")
    if (DefaultNativePlatform.getCurrentOperatingSystem().isMacOsX) {
        jvmArgs("-Dapple.awt.application.appearance=system")
    }
}

val runDebug by tasks.registering(JavaExec::class) {
    commonRun()

    description = "Run BootstrapDebug"
    mainClass = "net.legacylauncher.bootstrap.BootstrapDebug"

    args("--debug")

    val librariesDir by projects.launcher.dependencyProject.tasks.named<Sync>("buildLauncherRepo")
    val launcherJar by projects.launcher.dependencyProject.tasks.named<Jar>("jar")
    dependsOn(librariesDir, launcherJar, devJar)
    environment("LL_LAUNCHER_JAR", launcherJar.archiveFile.get().asFile)
    environment("LL_LIBRARIES_DIR", librariesDir.destinationDir)

    if (System.getenv("JRE_LEGACY") == "true") {
        javaLauncher = javaToolchains.launcherFor {
            languageVersion = JavaLanguageVersion.of(8)
        }
        classpath(dev.runtimeClasspath)
    } else {
        classpath(java11.runtimeClasspath, dev.runtimeClasspath)
    }
}

val runRelease by tasks.registering(JavaExec::class) {
    commonRun()

    description = "Run Bootstrap"
    mainClass = "org.springframework.boot.loader.JarLauncher"

    val librariesDir by projects.launcher.dependencyProject.tasks.named<Sync>("buildLauncherRepo")
    val launcherJar by projects.launcher.dependencyProject.tasks.named<Jar>("jar")
    dependsOn(librariesDir, launcherJar)

    args(
        "--ignoreUpdate", "--ignoreSelfUpdate",
        "--targetJar", launcherJar.archiveFile.get().asFile,
        "--targetLibFolder", librariesDir.destinationDir,
        "--",
        "--debug"
    )

    classpath(bootJar)

    if (System.getenv("JRE_LEGACY") == "true") {
        javaLauncher = javaToolchains.launcherFor {
            languageVersion = JavaLanguageVersion.of(8)
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

buildConfig {
    className("BuildConfig")
    packageName("net.legacylauncher.bootstrap")

    useJavaOutput()

    buildConfigField("String", "SHORT_BRAND", brand.brand.map { "\"$it\"" })
    buildConfigField("String", "FULL_BRAND", brand.displayName.map { "\"$it\"" })
    buildConfigField("String", "VERSION", brand.version.map { "\"$it\"" })
}

val processBootResources by tasks.getting(ProcessResources::class) {
    val meta = mapOf(
        "version" to brand.version.get(),
        "shortBrand" to brand.brand.get(),
        "brand" to brand.displayName.get(),
    )

    inputs.property("meta", meta)

    doLast {
        val file = destinationDir.resolve("META-INF/bootstrap-meta.json")
        file.parentFile.mkdirs()
        file.writer().use { writer ->
            ObjectMapper().writeValue(writer, meta)
        }
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
    dependsOn(bootJar)
    inputs.property("productVersion", brand.version.get())
    inputs.property("repoHosts", brand.repoHosts.get())
    val updateJsonFile = layout.buildDirectory.file("update/${brand.brand.get()}/bootstrap.json")
    outputs.file(updateJsonFile)

    doLast {
        val jarFileChecksum = generateChecksum(bootJar.outputs.files.singleFile)
        val downloadPath = "repo/update/${brand.brand.get()}/bootstrap/${jarFileChecksum}.jar"
        val meta = mapOf(
            "version" to brand.version.get(),
            "checksum" to jarFileChecksum,
            "url" to brand.repoDomains.get().map { domain ->
                "https://$domain/$downloadPath"
            } + brand.repoHosts.get().flatMap { host ->
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
    from(bootJar)
    into(layout.buildDirectory.dir("update/${brand.brand.get()}"))
    rename { "bootstrap.jar" }
}

val generateSha256File by tasks.registering {
    dependsOn(bootJar)
    val file = layout.buildDirectory.file("update/${brand.brand.get()}/bootstrap.jar.sha256")
    outputs.file(file)
    doLast {
        file.get().asFile.writeText(generateChecksum(bootJar.outputs.files.singleFile))
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