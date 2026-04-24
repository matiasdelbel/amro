plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.amro.movies.data"
}

dependencies {
    implementation(projects.core.coreCommon)
    implementation(projects.core.coreCommonDi)
    implementation(projects.core.coreNetwork)
    implementation(projects.features.movies.domain)

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(projects.core.coreTesting)
    testImplementation(libs.ktor.client.mock)
}
