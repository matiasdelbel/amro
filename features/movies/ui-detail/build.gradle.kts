plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.amro.roborazzi)
}

android {
    namespace = "com.amro.movies.detail"

    buildFeatures { compose = true }
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.designSystem)
    implementation(projects.features.movies.domain)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)

    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(projects.core.testing)
}
