plugins {
    `java-library`
}

dependencies {
    implementation(libs.bundles.dbus)
    implementation(libs.junixsocket.core)
}