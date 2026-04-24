plugins {
    alias(libs.plugins.amro.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.amro.core.coroutine"
}

dependencies {
    api(libs.kotlinx.coroutines.core)
    api(libs.javax.inject)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
