import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

// TMDB credentials are looked up in two places, in order:
//   1. `local.properties` at the project root (recommended: never committed)
//   2. project Gradle properties (gradle.properties / -P flags / env GRADLE_* overrides)
// This way each developer can drop a `TMDB_API_KEY=...` line in local.properties without
// touching the codebase, and CI can pass `-PTMDB_API_KEY=...` to override.
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun tmdbProp(name: String, default: String): String =
    localProps.getProperty(name)
        ?: providers.gradleProperty(name).orNull
        ?: default

android {
    namespace = "com.amro.core.network"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        buildConfigField(
            "String",
            "TMDB_API_KEY",
            "\"${tmdbProp("TMDB_API_KEY", "YOUR_TMDB_API_KEY_HERE")}\"",
        )
        buildConfigField(
            "String",
            "TMDB_BASE_URL",
            "\"${tmdbProp("TMDB_BASE_URL", "https://api.themoviedb.org/3/")}\"",
        )
        buildConfigField(
            "String",
            "TMDB_IMAGE_BASE_URL",
            "\"${tmdbProp("TMDB_IMAGE_BASE_URL", "https://image.tmdb.org/t/p/")}\"",
        )
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    api(libs.ktor.client.core)
    api(libs.ktor.client.content.negotiation)
    api(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.logging)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.google.truth)
}
