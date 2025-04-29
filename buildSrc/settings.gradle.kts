rootProject.name = "buildSrc"

// Enable Version Catalog in buildSrc
// See: https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
