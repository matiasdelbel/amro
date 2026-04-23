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
    implementation(project(":features:movies:movies-domain"))
    implementation(project(":core:core-common"))
    implementation(project(":core:core-network"))

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(project(":core:core-testing"))
    testImplementation(libs.ktor.client.mock)
}
