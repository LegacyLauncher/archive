val versionBuffer = java.io.ByteArrayOutputStream()

val versionResult = exec {
    commandLine("git", "describe", "--tags", "--always", "--match", "[0-9].*")
    standardOutput = versionBuffer
}

project.version = when (versionResult.exitValue) {
    0 -> versionBuffer.toByteArray().toString(Charsets.UTF_8).trim()
    else -> "detached"
}
