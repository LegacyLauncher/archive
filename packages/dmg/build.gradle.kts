import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.Verify
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    base
    alias(libs.plugins.download)
    net.legacylauncher.brand
}

val jreZipDownload = "https://cdn.azul.com/zulu/bin/zulu21.32.17-ca-fx-jre21.0.2-macosx_x64.zip"
val jreZipSha256 = "49c9ab085278660c7f3236a70be07a9d15077ce6815f97239d3d3a066c6ad1dd"
val jreZipEntry = "zulu21.32.17-ca-fx-jre21.0.2-macosx_x64/zulu-21.jre"
val jreZipFile = layout.buildDirectory.file("jreZip/macOsJre.zip")

val bundleName = "Legacy Launcher ${brand.displayName.get()}"

evaluationDependsOn(projects.launcher.identityPath.path)

val tokens = mapOf(
    "bundle_name" to bundleName,
    "short_brand" to brand.brand.get(),
    "full_brand" to brand.displayName.get(),
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
    into(layout.buildDirectory.dir("dmg/${brand.brand.get()}"))

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
            val jar by projects.bootstrap.dependencyProject.tasks.named("bootJar", AbstractArchiveTask::class)
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