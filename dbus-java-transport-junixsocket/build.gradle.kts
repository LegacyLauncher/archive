plugins {
    `java-library`
    `auto-version`
}

dependencies {
    implementation(libs.bundles.dbus)
    implementation(libs.junixsocket.core)
}