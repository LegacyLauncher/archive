import de.undercouch.gradle.tasks.download.Download
import org.apache.tools.ant.filters.ReplaceTokens
import java.util.*

plugins {
    base
}

evaluationDependsOn(projects.launcher.identityPath.path)

val shortBrand: String by rootProject.ext
val fullBrand: String by rootProject.ext

val mainIss = mapOf(
    "name" to "Legacy Launcher $fullBrand",
    "version" to "${projects.launcher.version}.0",
    "id" to generateUUIDFromString(shortBrand),
    "short_brand" to shortBrand,
)

val prepareInstaller by tasks.registering(Sync::class) {
    into(layout.buildDirectory.file("innosetup/$shortBrand"))

    val portable = projects.packages.portable.dependencyProject

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

        val x64 = portable.tasks.named("downloadJreX64", Download::class)
        val x86 = portable.tasks.named("downloadJreX86", Download::class)
        dependsOn(x64, x86)

        includeEmptyDirs = false

        into("x64/jre") {
            from(zipTree(x64.get().dest)) {
                eachFile {
                    relativePath = relativePath.dropSegments(3..3)
                }
            }
        }

        into("x86/jre/x86") {
            from(zipTree(x86.get().dest)) {
                eachFile {
                    relativePath = relativePath.dropSegments(4..4)
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
        layout.buildDirectory.dir("update/$shortBrand").get().asFile.mkdirs()
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