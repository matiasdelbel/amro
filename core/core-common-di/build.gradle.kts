plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.amro.core.common.di"
}

dependencies {
    api(projects.core.coreCommon)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
