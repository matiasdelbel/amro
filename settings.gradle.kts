@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    includeBuild("build-logic")

    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AMRO"

include(":app")

// Core modules
include(":core:coroutine")
include(":core:design-system")
include(":core:domain")
include(":core:network")
include(":core:testing")

// Feature: Movies
include(":features:movies:domain")
include(":features:movies:data")
include(":features:movies:ui-listing")
include(":features:movies:ui-detail")
