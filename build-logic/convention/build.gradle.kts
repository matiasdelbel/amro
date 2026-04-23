plugins {
    `kotlin-dsl`
}

group = "com.amro.convention"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.roborazzi.gradle.plugin)
    compileOnly(libs.roborazzi.core.jvm)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "amro.android.library"
            implementationClass = "com.amro.convention.AndroidLibraryConventionPlugin"
        }
        register("roborazzi") {
            id = "amro.roborazzi"
            implementationClass = "com.amro.convention.RoborazziConventionPlugin"
        }
    }
}
