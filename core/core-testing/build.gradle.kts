plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    api(project(":core:core-common"))
    api(libs.kotlinx.coroutines.test)
    api(libs.junit)
    api(libs.mockk)
    api(libs.turbine)
    api(libs.google.truth)
}
