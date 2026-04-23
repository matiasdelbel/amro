plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.amro.roborazzi)
}

android {
    namespace = "com.amro.designsystem"

    buildFeatures { compose = true }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.material3)
    api(libs.androidx.compose.material.icons.extended)
    api(libs.androidx.compose.ui)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.tooling.preview)
    api(libs.coil.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
