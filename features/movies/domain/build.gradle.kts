plugins {
    alias(libs.plugins.amro.android.library)
}

android {
    namespace = "com.amro.movies.domain"
}

dependencies {
    api(projects.core.domain)
    implementation(libs.javax.inject)

    testImplementation(projects.core.testing)
}
