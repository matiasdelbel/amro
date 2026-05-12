plugins {
    alias(libs.plugins.amro.android.library)
}

android {
    namespace = "com.amro.movies.domain"
}

dependencies {
    api(projects.core.coroutine)
    api(projects.core.domain)

    implementation(libs.javax.inject)
    implementation(libs.kotlinx.coroutines.core)

    testImplementation(projects.core.testing)
}
