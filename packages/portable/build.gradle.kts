import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.configurationcache.extensions.capitalized
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

plugins {
    base
    alias(libs.plugins.download)
    net.legacylauncher.brand
}

val portableBaseBuildDir = layout.buildDirectory.dir("portableBase/${brand.brand.get()}")

val jreVersions = mapOf(
    "x64" to mapOf(
        "url" to "https://cdn.azul.com/zulu/bin/zulu21.32.17-ca-fx-jre21.0.2-win_x64.zip",
        "sha256" to "a4a803b5091d9200e508019d3a0090bf0e2a2f74ea752cf0622db93583d390d0",
    ),
    "x86" to mapOf(
        "url" to "https://cdn.azul.com/zulu/bin/zulu17.48.15-ca-fx-jre17.0.10-win_i686.zip",
        "sha256" to "7208a3fadae4897ccc33e7bafad8c5c1f4699376fb2910d82cc3d42498bb9f4d",
    ),
)

jreVersions.forEach { (arch, jre) ->
    val jreZip = layout.buildDirectory.file("jreDownloads/jre${arch.capitalized()}.zip")
    val verifyTask by tasks.register("verifyJre${arch.capitalized()}", Verify::class) {
        src(jreZip)
        algorithm("SHA-256")
        checksum(jre["sha256"])
    }
    val downloadTask by tasks.register("downloadJre${arch.capitalized()}", Download::class) {
        src(jre["url"])
        dest(jreZip)
        overwrite(false)
        finalizedBy(verifyTask)
    }
}

val preparePortableBaseBuild by tasks.registering(Sync::class) {
    into(layout.buildDirectory.dir("portableBase/${brand.brand.get()}"))

    from(file("baseResources"))

    into("launcher") {
        from(projects.bootstrap.dependencyProject.tasks.named("shadowJar", AbstractArchiveTask::class)) {
            rename { "bootstrap.jar" }
        }
        from(projects.launcher.dependencyProject.tasks.named("jar", AbstractArchiveTask::class)) {
            rename { "launcher.jar" }
        }
        into("libraries") {
            from(projects.launcher.dependencyProject.tasks.named("buildLauncherRepo", AbstractCopyTask::class))
        }
    }
}

val preparePortableBuild by tasks.registering(Sync::class) {
    into(layout.buildDirectory.dir("portable/${brand.brand.get()}"))

    from(preparePortableBaseBuild)

    into("jre") {
        val x64 = tasks.named("downloadJreX64", Download::class)
        val x86 = tasks.named("downloadJreX86", Download::class)
        dependsOn(x64, x86)

        includeEmptyDirs = false

        from(zipTree(x64.get().dest)) {
            eachFile {
                relativePath = relativePath.dropSegments(1..1)
            }
        }

        into("x86") {
            from(zipTree(x86.get().dest)) {
                eachFile {
                    relativePath = relativePath.dropSegments(2..2)
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

val createPortableBuild by tasks.registering {
    dependsOn(zipPortableBuild)
}

val assemble: Task by tasks.getting {
    if (System.getenv("PORTABLE_ENABLED") == "true") {
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