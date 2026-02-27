plugins {
    id("java-library")
    id("maven-publish")
}

dependencies {
    implementation(project(":common"))
    implementation(project(":utils"))

    // JSON processing
    implementation(libs.gson)

    // HTTP client
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Logging
    implementation(libs.slf4j.api)
    implementation(libs.log4j.slf4j.impl)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "net.legacylauncher"
            artifactId = "mods-manager"
            version = project.version.toString()
            from(components["java"])
        }
    }
}