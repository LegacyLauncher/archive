import de.undercouch.gradle.tasks.download.*
import net.legacylauncher.gradle.*
import net.legacylauncher.gradle.LegacyLauncherS3UploadTask.Companion.bucketProperty
import org.apache.tools.ant.filters.*
import java.time.*
import java.time.format.*
import java.util.*

plugins {
    `java-base` // required for corrent variant aware dependency resolution
    alias(libs.plugins.download)
    net.legacylauncher.brand
}

val bootstrapJar: Configuration by configurations.creating {
    isCanBeDeclared = true
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(LegacyLauncherPackaging.ATTRIBUTE, objects.named(LegacyLauncherPackaging.BOOTSTRAP_JAR))
    }
}

val launcherJar: Configuration by configurations.creating {
    isCanBeDeclared = true
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes {
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(LegacyLauncherPackaging.ATTRIBUTE, objects.named(LegacyLauncherPackaging.LAUNCHER_JAR))
    }
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

dependencies {
    bootstrapJar(projects.bootstrap)
    launcherJar(projects.launcher)
    launcherLibraries(projects.launcher)
}

PORTABLE_WIN_ARCHITECTURES.forEach { pkg ->
    val jreZip = layout.buildDirectory.file("jreDownloads/jre${pkg.archCapitalized}.zip")
    val verifyTask by tasks.register(pkg.verifyTaskName, Verify::class) {
        src(jreZip)
        algorithm("SHA-256")
        checksum(pkg.jre.sha256)
    }
    tasks.register(pkg.downloadTaskName, Download::class) {
        src(pkg.jre.url)
        dest(jreZip)
        overwrite(false)
        finalizedBy(verifyTask)
    }
}

val preparePortableBaseBuild by tasks.registering(Sync::class) {
    into(layout.buildDirectory.dir("portableBase/${brand.brand.get()}"))

    from(file("baseResources"))

    into("launcher") {
        from(bootstrapJar) {
            rename { "bootstrap.jar" }
        }
        from(launcherJar) {
            rename { "launcher.jar" }
        }
        into("libraries") {
            launcherLibraries.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
                val path = with(artifact.moduleVersion.id) {
                    "${group.replace('.', '/')}/$name/$version"
                }
                into(path) {
                    from(artifact.file)
                }
            }
        }
    }
}

val preparePortableBuild by tasks.registering(Sync::class) {
    into(layout.buildDirectory.dir("portable/${brand.brand.get()}"))

    from(preparePortableBaseBuild)

    into("jre") {
        includeEmptyDirs = false

        PORTABLE_WIN_ARCHITECTURES.forEach { pkg ->
            val task = tasks.named(pkg.downloadTaskName, Download::class)
            dependsOn(task)

            into(pkg.arch) {
                from(task.map { zipTree(it.dest) }) {
                    eachFile {
                        relativePath = relativePath.dropSegments(2..2)
                    }
                }
            }
        }
    }

    from("resources") {
        inputs.file("tl.args.0.txt")
        inputs.file("tl.args.1.txt")

        filter<ReplaceTokens>(
            "tokens" to mapOf(
                "date" to DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
                    .withLocale(Locale.ROOT)
                    .format(LocalDate.now(ZoneOffset.UTC)),
                "version" to projects.launcher.version,
                "bootstrap_args" to file("tl.args.0.txt").readText().trim(),
                "launcher_args" to file("tl.args.1.txt").readText().trim(),
                "launcher_properties" to "minecraft.jre.dir=./jre\n"
            )
        )

        filteringCharset = "UTF-8"
    }
}

val zipPortableBuild by tasks.registering(Zip::class) {
    from(preparePortableBuild)
    destinationDirectory = layout.buildDirectory.dir("update/${brand.brand.get()}")
    archiveFileName = "portable.zip"
    isPreserveFileTimestamps = true
}

val portableEnabled = System.getenv("PORTABLE_ENABLED") == "true"

val createPortableBuild by tasks.registering {
    enabled = portableEnabled
    dependsOn(zipPortableBuild)
    outputs.files(zipPortableBuild)
}

val deploy by tasks.registering(LegacyLauncherS3UploadTask::class) {
    enabled = portableEnabled
    bucket = project.bucketProperty(LegacyLauncherS3UploadTask.Companion.Buckets.BRANDS)
    entityPrefix = "portable"
    inputs.files(createPortableBuild)
}

val assemble: Task by tasks.getting {
    if (portableEnabled) {
        dependsOn(createPortableBuild)
    }
    doLast {
        zipPortableBuild.get().destinationDirectory.get().asFile.mkdirs()
    }
}

fun RelativePath.dropSegments(range: IntRange): RelativePath {
    val segments = segments.filterIndexed { idx, _ ->
        idx !in range
    }
    return RelativePath(isFile, *segments.toTypedArray())
}
