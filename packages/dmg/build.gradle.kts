import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    base
    id("de.undercouch.download")
}

val jreZipDownload = "https://cdn.azul.com/zulu/bin/zulu17.38.21-ca-fx-jre17.0.5-macosx_x64.zip"
val jreZipSha256 = "7483050a347894c13a6538dc42a1bfaad75939784c92f1e67433ca9a16929547"
val jreZipEntry = "zulu17.38.21-ca-fx-jre17.0.5-macosx_x64/zulu-17.jre"
val jreZipFile = layout.buildDirectory.file("jreZip/macOsJre.zip")

val shortBrand: String by rootProject.ext
val fullBrand: String by rootProject.ext

val bundleName = "Legacy Launcher $fullBrand"

evaluationDependsOn(projects.launcher.identityPath.path)

val tokens = mapOf(
    "bundle_name" to bundleName,
    "short_brand" to shortBrand,
    "full_brand" to fullBrand,
    "version" to projects.launcher.version
)

val verifyMacOsJre by tasks.registering(Verify::class) {
    src(jreZipFile)
    algorithm("SHA-256")
    checksum(jreZipSha256)
}

val downloadMacOsJre by tasks.registering(Download::class) {
    src(jreZipDownload)
    dest(jreZipFile)
    overwrite(false)
    finalizedBy(verifyMacOsJre)
}

val prepareDmgBuild by tasks.registering(Sync::class) {
    into(layout.buildDirectory.dir("dmg/$shortBrand"))

    from("TL.icns")
    from("background/background.tiff")
    from("script") {
        filter<ReplaceTokens>("tokens" to tokens)
        filteringCharset = "UTF-8"
    }

    into("$bundleName.app/Contents") {
        from("contents/binary")
        from("contents/textual") {
            filter<ReplaceTokens>("tokens" to tokens)
            filteringCharset = "UTF-8"
        }

        into("Resources") {
            from("TL.icns")
        }

        into("app") {
            val jar by projects.bootstrap.dependencyProject.tasks.named("shadowJar", AbstractArchiveTask::class)
            from(jar) {
                rename { "bootstrap.jar" }
            }
        }

        into("runtime") {
            dependsOn(downloadMacOsJre)
            from(zipTree(downloadMacOsJre.get().dest)) {
                eachFile {
                    if (relativePath.segments.size <= 5) exclude()
                    else relativePath = relativePath.dropSegments(3..4)
                }
                includeEmptyDirs = false
            }
        }
    }
}

val assemble: Task by tasks.getting {
    if (System.getenv("DMG_ENABLED") == "true") {
        dependsOn(prepareDmgBuild)
    }
    doLast {
        prepareDmgBuild.get().destinationDir.mkdirs()
    }
}

fun RelativePath.dropSegments(range: IntRange): RelativePath {
    val segments = segments.filterIndexed { idx, _ ->
        idx !in range
    }
    return RelativePath(isFile, *segments.toTypedArray())
}