package net.legacylauncher.gradle

data class PortableWinJreInfo(val url: String, val sha256: String)
data class PortableWinArchPackageInfo(val arch: String, val jre: PortableWinJreInfo) {
    val archCapitalized = arch.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    val downloadTaskName = "download$archCapitalized"
    val verifyTaskName = "verify$archCapitalized"
}

val PORTABLE_WIN_ARCHITECTURES = listOf(
    PortableWinArchPackageInfo(
        "x64",
        PortableWinJreInfo(
            "https://cdn.azul.com/zulu/bin/zulu21.32.17-ca-fx-jre21.0.2-win_x64.zip",
            "a4a803b5091d9200e508019d3a0090bf0e2a2f74ea752cf0622db93583d390d0",
        )
    ),
    PortableWinArchPackageInfo(
        "arm64",
        PortableWinJreInfo(
            "https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.7%2B6/OpenJDK21U-jre_aarch64_windows_hotspot_21.0.7_6.zip",
            "0e2d89e3d66739ffff74f7ed9582ebe3f7748898a0eaec96164e4ec5e8236b28"
        )
    )
)
