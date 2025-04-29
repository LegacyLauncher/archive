import de.undercouch.gradle.tasks.download.*
import net.legacylauncher.gradle.PORTABLE_WIN_ARCHITECTURES
import org.apache.tools.ant.filters.*
import java.util.*

plugins {
    base
    alias(libs.plugins.download)
    net.legacylauncher.brand
}

evaluationDependsOn(projects.launcher.path)

val mainIss = mapOf(
    "name" to "Legacy Launcher ${brand.displayName.get()}",
    "version" to "${projects.launcher.version}.0",
    "id" to generateUUIDFromString(brand.brand.get()),
    "short_brand" to brand.brand.get(),
)

val prepareInstaller by tasks.registering(Sync::class) {
    into(layout.buildDirectory.file("innosetup/${brand.brand.get()}"))

    val portable = project(projects.packages.portable.path)

    into("files") {
        into("common") {
            from(portable.tasks.named("preparePortableBaseBuild"))

            from("launcherResources") {
                inputs.file(portable.file("tl.args.0.txt"))
                inputs.file(portable.file("tl.args.1.txt"))

                filter<ReplaceTokens>(
                    "tokens" to mapOf(
                        "bootstrap_args" to portable.file("tl.args.0.txt").readText().trim(),
                        "launcher_args" to portable.file("tl.args.1.txt").readText().trim(),
                    )
                )

                filteringCharset = "UTF-8"
            }
        }

        includeEmptyDirs = false

        PORTABLE_WIN_ARCHITECTURES.forEach { pkg ->
            val download = portable.tasks.named(pkg.downloadTaskName, Download::class)
            dependsOn(download)

            into("${pkg.arch}/jre/${pkg.arch}") {
                from(download.map { zipTree(it.dest) }) {
                    eachFile {
                        relativePath = relativePath.dropSegments(4..4)
                    }
                }
            }
        }
    }

    from("resources")

    from("main.iss") {
        filter<ReplaceTokens>(
            "tokens" to mainIss
        )

        filteringCharset = "UTF-8"
    }
}

val assemble: Task by tasks.getting {
    if (System.getenv("INSTALLER_ENABLED") == "true") {
        dependsOn(prepareInstaller)
    }

    doLast {
        prepareInstaller.get().destinationDir.mkdirs()
        layout.buildDirectory.dir("update/${brand.brand.get()}").get().asFile.mkdirs()
    }
}

fun generateUUIDFromString(seed: String): String {
    val random = java.util.Random(seed.hashCode().toLong())
    val uuidBytes = ByteArray(16)
    random.nextBytes(uuidBytes)
    return UUID.nameUUIDFromBytes(uuidBytes).toString()
}

fun RelativePath.dropSegments(range: IntRange): RelativePath {
    val segments = segments.filterIndexed { idx, _ ->
        idx !in range
    }
    return RelativePath(isFile, *segments.toTypedArray())
}
