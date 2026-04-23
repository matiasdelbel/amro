package com.amro.convention

import com.android.build.gradle.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

/**
 * Baseline convention for every Android library module in the repo.
 *
 * Applies the Android Library + Kotlin Android plugins and sets the SDK + JVM toolchain defaults
 * shared by all library modules, so individual `build.gradle.kts` files only need to declare the
 * things that are actually unique to them (namespace, Compose opt-in, dependencies).
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            with(pluginManager) {
                apply(libs.findPlugin(AliasPluginAndroidLibrary).get().get().pluginId)
                apply(libs.findPlugin(AliasPluginKotlinAndroid).get().get().pluginId)
            }

            extensions.configure<LibraryExtension> {
                compileSdk = libs.findVersion(ConfigCompileSdk).get().requiredVersion.toInt()

                defaultConfig {
                    minSdk = libs.findVersion(ConfigMinSdk).get().requiredVersion.toInt()
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_11
                    targetCompatibility = JavaVersion.VERSION_11
                }
            }

            tasks.withType<KotlinCompile>().configureEach {
                compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
            }
        }
    }

    companion object {
        const val AliasPluginAndroidLibrary = "android-library"
        const val AliasPluginKotlinAndroid = "kotlin-android"

        const val ConfigCompileSdk = "compileSdk"
        const val ConfigMinSdk = "minSdk"
    }
}
