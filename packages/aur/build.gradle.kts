import org.apache.tools.ant.filters.ReplaceTokens
import java.security.MessageDigest

plugins {
    base
}

val pkgInfo = mutableMapOf<String, Any?>()

// If you'd like to change these, check resource files, too
val appDir = "/opt/legacylauncher"
val execPath = "/usr/bin/legacylauncher"

evaluationDependsOn(projects.bootstrap.identityPath.path)
evaluationDependsOn(projects.launcher.identityPath.path)

val shortBrand: String by rootProject.ext
val repoCdnPathPrefixes: Collection<String> by rootProject.ext
val repoHosts: Collection<String> by rootProject.ext

val collectPkgBuildInfo by tasks.registering {
    dependsOn(projects.bootstrap.dependencyProject.tasks.named("assemble"))
    dependsOn(projects.launcher.dependencyProject.tasks.named("assemble"))

    doLast {
        val alternateRepoComment = repoCdnPathPrefixes.map { cdnPath ->
            "#_repo='${cdnPath}/repo'"
        } + repoHosts.map { host ->
            "#_repo='https://${host}/repo'"
        }

        val checksums = mutableListOf<String>()
        val launcherLibs = mutableListOf<String>()
        val launcherLibsInstalls = mutableListOf<String>()
        val resourceList = mutableListOf<String>()

        val launcherClasspath: Provider<Collection<ResolvedArtifact>> by projects.launcher.dependencyProject.ext

        launcherClasspath.get().forEach { artifact ->
            val artifactId = artifact.moduleVersion.id
            val fileName = when {
                artifact.classifier != null -> "${artifactId.name}-${artifactId.version}-${artifact.classifier}.${artifact.extension}"
                else -> "${artifactId.name}-${artifactId.version}.${artifact.extension}"
            }
            val path = "${artifactId.group.replace('.', '/')}/${artifactId.name}/${artifactId.version}/$fileName"
            launcherLibs += "\"\${_repo}/libraries/${path}\""
            launcherLibsInstalls += "install -Dm0644 \"\${srcdir}/${fileName}\" \"\${pkgdir}${appDir}/lib/${path}\""
            checksums += "'${generateChecksum(artifact.file)}' # ${artifact.moduleVersion.id}"
        }

        fileTree("resources").visit {
            if (isDirectory) return@visit
            val path = relativePath.pathString
            resourceList += path
            checksums += "'${generateChecksum(file)}' # $path"
        }

        val bootstrapVersion = projects.bootstrap.version
        val launcherVersion = projects.launcher.version
        val launcherChecksum = generateChecksum(
            projects.launcher.dependencyProject.tasks.named("jar", AbstractArchiveTask::class)
                .get().outputs.files.singleFile
        )
        val bootstrapChecksum = generateChecksum(
            projects.bootstrap.dependencyProject.tasks.named("shadowJar", AbstractArchiveTask::class)
                .get().outputs.files.singleFile
        )

        pkgInfo += mapOf(
            "PKGNAME" to (System.getenv("PKGBUILD_PKGNAME") ?: "legacylauncher"),
            "PKGDESC" to (System.getenv("PKGBUILD_PKGDESC") ?: "Freeware Minecraft launcher"),
            "PKGREL" to "1",
            "BRAND" to shortBrand,
            "LAUNCHER_VERSION" to launcherVersion,
            "ALTERNATE_REPO_COMMENT" to alternateRepoComment.joinToString("\n"),
            "LAUNCHER_CHECKSUM" to launcherChecksum,
            "BOOTSTRAP_CHECKSUM" to bootstrapChecksum,
            "BOOTSTRAP_VERSION" to bootstrapVersion,
            "LAUNCHER_LIBRARIES" to launcherLibs.joinToString("\n") { "  $it" },
            "RESOURCE_LIST" to resourceList.joinToString("\n") { "  $it" },
            "CHECKSUMS" to checksums.joinToString("\n") { "  $it" },
            "LAUNCHER_LIBRARIES_INSTALLS" to launcherLibsInstalls.joinToString("\n") { "  $it" },
            "BOOTSTRAP_JAR" to "legacylauncher-bootstrap-${bootstrapVersion}-${bootstrapChecksum.take(8)}.jar",
            "LAUNCHER_JAR" to "legacylauncher-launcher-${launcherVersion}-${launcherChecksum.take(8)}.jar",
            "APP_DIR" to appDir,
            "EXEC_PATH" to execPath,
        )
    }
}

val createPkgBuild by tasks.registering(Sync::class) {
    dependsOn(collectPkgBuildInfo)
    into(layout.buildDirectory.dir("aur/${shortBrand}"))

    from("PKGBUILD") {
        filter<ReplaceTokens>("tokens" to pkgInfo)
        filteringCharset = "UTF-8"
    }
    from("resources")
    from(resources.text.fromString(projects.launcher.version!!)) {
        rename { ".pkgver" }
    }
    from(resources.text.fromString("1")) {
        rename { ".pkgrel" }
    }
}

val assemble: Task by tasks.getting {
    if (System.getenv("PKGBUILD_ENABLED") == "true") {
        dependsOn(createPkgBuild)
    }
    doLast {
        createPkgBuild.get().destinationDir.mkdirs()
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
