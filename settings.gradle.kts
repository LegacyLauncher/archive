plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "legacy-launcher"

include(":bootstrap")
include(":bridge")
include(":common")
include(":dbus-java-transport-junixsocket")
include(":launcher")
include(":packages:aur")
include(":packages:dmg")
include(":packages:installer")
include(":packages:portable")
