plugins {
    alias(libs.plugins.amro.android.library)
}

android {
    namespace = "com.amro.core.testing"
}

dependencies {
    api(projects.core.coroutine)
    api(libs.kotlinx.coroutines.test)
    api(libs.junit)
    api(libs.mockk)
    api(libs.turbine)
    api(libs.google.truth)
}
