import com.fasterxml.jackson.databind.*
import net.legacylauncher.gradle.*
import net.legacylauncher.gradle.LegacyLauncherS3UploadTask.Companion.bucketProperty
import org.gradle.nativeplatform.platform.internal.*
import org.springframework.boot.gradle.tasks.bundling.*
import java.nio.charset.*
import java.security.*

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

sourceSets.test {
    compileClasspath += java11.compileClasspath
    runtimeClasspath += java11.runtimeClasspath
    compileClasspath += java11.output
}

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

    "java11CompileOnly"(projects.common) {
        capabilities {
            requireFeature("java11")
        }
    }
    "java11Implementation"(libs.bundles.dbus)
    "java11Implementation"(libs.junixsocket.core)
    "java11Implementation"(projects.dbusJavaTransportJunixsocket)
    "java11Implementation"(libs.system.theme.detector)

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

val launcherLibraries: Configuration by configurations.creating {
    isCanBeDeclared = true
    isCanBeResolved = true
    isCanBeConsumed = false

    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(LegacyLauncherPackaging.ATTRIBUTE, objects.named(LegacyLauncherPackaging.LAUNCHER_LIBRARY))
    }
}

val launcherJar: Configuration by configurations.creating {
    isCanBeDeclared = true
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(LegacyLauncherPackaging.ATTRIBUTE, objects.named(LegacyLauncherPackaging.LAUNCHER_JAR))
    }
}

dependencies {
    launcherLibraries(projects.launcher)
    launcherJar(projects.launcher)
}

val collectLauncherLibsRepo by tasks.registering(Sync::class) {
    dependsOn(launcherLibraries)
    into(layout.buildDirectory.dir("launcherLibs"))
    launcherLibraries.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
        val path = with(artifact.moduleVersion.id) {
            "${group.replace('.', '/')}/$name/$version"
        }
        into(path) {
            from(artifact.file)
        }
    }
}

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

//    jvmArgs("-DsocksProxyHost=127.0.0.1", "-DsocksProxyPort=2081")
    args("--debug")

    dependsOn(collectLauncherLibsRepo, launcherJar, devJar)

    environment("LL_LAUNCHER_JAR", launcherJar.resolve().single())
    environment("LL_LIBRARIES_DIR", collectLauncherLibsRepo.get().destinationDir)

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

    dependsOn(collectLauncherLibsRepo, launcherJar)

    args(
        "--ignoreUpdate", "--ignoreSelfUpdate",
        "--targetJar", launcherJar.resolve().single(),
        "--targetLibFolder", collectLauncherLibsRepo.get().destinationDir,
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

val generateUpdateJson by tasks.registering {
    dependsOn(bootJar)
    inputs.property("productVersion", brand.version)
    val updateJsonFile = layout.buildDirectory.file("update/${brand.brand.get()}/bootstrap.json")
    outputs.file(updateJsonFile)

    doLast {
        val jarFileChecksum = generateChecksum(bootJar.outputs.files.singleFile)
        val meta = mapOf(
            "version" to brand.version.get(),
            "checksum" to jarFileChecksum,
        )

        updateJsonFile.get().asFile.writer().use { writer ->
            ObjectMapper().writeValue(writer, meta)
        }
    }
}

val copyJarAndRename by tasks.registering {
    dependsOn(bootJar)
    val targetFile = layout.buildDirectory.file("update/${brand.brand.get()}/bootstrap.jar")
    outputs.file(targetFile)
    doLast {
        bootJar.outputs.files.singleFile.copyTo(targetFile.get().asFile, overwrite = true)
    }
}

val generateSha256File by tasks.registering {
    dependsOn(bootJar)
    val file = layout.buildDirectory.file("update/${brand.brand.get()}/bootstrap.jar.sha256")
    outputs.file(file)
    doLast {
        file.get().asFile.writeText(generateChecksum(bootJar.outputs.files.singleFile))
    }
}

val prepareBootstrapDeploy by tasks.registering {
    dependsOn(generateSha256File, copyJarAndRename, generateUpdateJson)
    outputs.files(dependsOn)
}

val deployLauncherLibs by tasks.registering(LegacyLauncherS3UploadTask::class) {
    bucket = project.bucketProperty(LegacyLauncherS3UploadTask.Companion.Buckets.LIBS)
    brandPrefix = ""
    entityPrefix = ""
    inputs.files(collectLauncherLibsRepo)
}

val deployBootstrapBrand by tasks.registering(LegacyLauncherS3UploadTask::class) {
    mustRunAfter(deployLauncherLibs)
    bucket = project.bucketProperty(LegacyLauncherS3UploadTask.Companion.Buckets.BRANDS)
    entityPrefix = "bootstrap"
    inputs.files(prepareBootstrapDeploy)
}

val deployBootstrapUpdate by tasks.registering(LegacyLauncherS3UploadTask::class) {
    dependsOn(prepareBootstrapDeploy)
    bucket = project.bucketProperty(LegacyLauncherS3UploadTask.Companion.Buckets.UPDATES)
    entityPrefix = "bootstrap"
    fileName = generateSha256File.map { it.outputs.files.singleFile.readText() + ".jar" }
    inputs.files(copyJarAndRename)
}

val deploy by tasks.registering {
    group = "deploy"
    dependsOn(deployBootstrapBrand, deployBootstrapUpdate, deployLauncherLibs)
}

val assemble: Task by tasks.getting {
    dependsOn(prepareBootstrapDeploy)
}

@Suppress("UnstableApiUsage")
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}

val bootstrapJar by configurations.consumable("bootstrapJar") {
    attributes {
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(LegacyLauncherPackaging.ATTRIBUTE, objects.named(LegacyLauncherPackaging.BOOTSTRAP_JAR))
    }
}

artifacts {
    add(bootstrapJar.name, bootJar)
}
