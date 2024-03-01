plugins {
    `java-library`
    `jvm-test-suite`
    `auto-version`
}

dependencies {
    api(libs.slf4j.api)
    api(libs.commons.lang3)
}

@Suppress("UnstableApiUsage")
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }
    }
}