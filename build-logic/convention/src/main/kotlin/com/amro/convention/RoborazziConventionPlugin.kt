package com.amro.convention

import com.android.build.api.variant.LibraryAndroidComponentsExtension
import com.android.build.gradle.LibraryExtension
import com.github.takahirom.roborazzi.ExperimentalRoborazziApi
import io.github.takahirom.roborazzi.RoborazziExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

/**
 * Wires a library module for Roborazzi-powered Compose preview screenshot testing.
 *
 * Responsibilities:
 *  - Applies the `io.github.takahirom.roborazzi` Gradle plugin.
 *  - Points `roborazzi.outputDir` at a `screenshots/` folder inside the consuming module so
 *    recorded PNGs live next to the source they document.
 *  - Enables the ComposablePreviewScanner-backed test generator (a Robolectric test class is
 *    generated at build time, one parameterized test per `@Preview`). Consumer modules don't
 *    need to declare `packages` — it defaults to the module's `android.namespace` and can still
 *    be overridden in the module's `build.gradle.kts` if a different scope is needed.
 *  - Pulls in the full Roborazzi + Robolectric + ComposablePreviewScanner + Compose test stack
 *    as `testImplementation` dependencies from the version catalog.
 *  - Forces Roborazzi's file-path strategy so generated screenshots are written relative to
 *    `outputDir` (default strategy writes them next to the test's working directory instead).
 */
class RoborazziConventionPlugin : Plugin<Project> {

    @OptIn(ExperimentalRoborazziApi::class)
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            pluginManager.apply(libs.findPlugin(AliasPluginRoborazzi).get().get().pluginId)

            // The convention plugin is only meaningful when applied to a module that already has
            // the Android Library + Compose plugins applied, so we assume `LibraryExtension`
            // exists.
            extensions.configure<LibraryExtension> {
                testOptions { unitTests.isIncludeAndroidResources = true }
            }

            extensions.configure<RoborazziExtension> {
                outputDir.set(layout.projectDirectory.dir("screenshots"))

                generateComposePreviewRobolectricTests {
                    enable.set(true)
                    // Most `@Preview` composables in this project are intentionally `private`
                    // (they exist to drive the IDE preview pane / screenshot tests), so pick
                    // them up by default.
                    includePrivatePreviews.set(true)
                    // Values are interpolated verbatim into `@Config(sdk = ..., qualifiers = ...)`,
                    // so `sdk` must be an array literal (IntArray) and `qualifiers` an expression.
                    robolectricConfig.set(
                        mapOf(
                            "sdk" to "[33]",
                            "qualifiers" to "RobolectricDeviceQualifiers.Pixel5",
                        ),
                    )
                }
            }

            // Default `packages` to the module's android namespace so consumer modules don't
            // have to repeat themselves. `finalizeDsl` is the right hook here:
            //   - It fires after the consumer's `android { namespace = ... }` has run, so we
            //     can read the final namespace value.
            //   - It's guaranteed to fire before AGP's variant computation, and Roborazzi only
            //     validates `packages` from its own `onVariants` callback — so the default we
            //     set here lands before the validation runs.
            // Modules can still override by setting their own `roborazzi { generateCompose... { packages = ... } }`.
            extensions.configure<LibraryAndroidComponentsExtension> {
                finalizeDsl { libraryExtension ->
                    val namespace = libraryExtension.namespace ?: return@finalizeDsl
                    extensions.configure<RoborazziExtension> {
                        generateComposePreviewRobolectricTests {
                            if (!packages.isPresent || packages.get().isEmpty()) {
                                packages.set(listOf(namespace))
                            }
                        }
                    }
                }
            }

            // Make `captureRoboImage("foo.png")` and the generated preview tests write into
            // `outputDir` instead of "wherever CWD happens to be".
            tasks.withType<Test>().configureEach {
                systemProperty(
                    "roborazzi.record.filePathStrategy",
                    "relativePathFromRoborazziContextOutputDirectory",
                )
            }

            dependencies {
                "testImplementation"(libs.findLibrary(AliasLibRoborazzi).get())
                "testImplementation"(libs.findLibrary(AliasLibRoborazziCompose).get())
                "testImplementation"(libs.findLibrary(AliasLibRoborazziJunitRule).get())
                "testImplementation"(libs.findLibrary(AliasLibRoborazziComposePreviewScannerSupport).get())
                "testImplementation"(libs.findLibrary(AliasLibComposablePreviewScannerAndroid).get())
                "testImplementation"(libs.findLibrary(AliasLibRobolectric).get())
                "testImplementation"(libs.findLibrary(AliasLibJunit).get())
                "testImplementation"(platform(libs.findLibrary(AliasLibAndroidxComposeBom).get()))
                "testImplementation"(libs.findLibrary(AliasLibAndroidxComposeUiTestJunit4).get())
                // `ui-tooling` brings the `@Preview` implementation that the generated tests
                // need at test-runtime.
                "testImplementation"(libs.findLibrary(AliasLibAndroidxComposeUiTooling).get())
            }
        }
    }

    companion object {
        const val AliasPluginRoborazzi = "roborazzi"

        const val AliasLibRoborazzi = "roborazzi"
        const val AliasLibRoborazziCompose = "roborazzi-compose"
        const val AliasLibRoborazziJunitRule = "roborazzi-junit-rule"
        const val AliasLibRoborazziComposePreviewScannerSupport = "roborazzi-compose-preview-scanner-support"
        const val AliasLibComposablePreviewScannerAndroid = "composable-preview-scanner-android"
        const val AliasLibRobolectric = "robolectric"
        const val AliasLibJunit = "junit"
        const val AliasLibAndroidxComposeBom = "androidx-compose-bom"
        const val AliasLibAndroidxComposeUiTestJunit4 = "androidx-compose-ui-test-junit4"
        const val AliasLibAndroidxComposeUiTooling = "androidx-compose-ui-tooling"
    }
}
