pluginManagement {
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
include(":core:core-common")
include(":core:core-network")
include(":core:core-testing")
include(":core:design-system")

// Feature: Movies
include(":features:movies:movies-domain")
include(":features:movies:movies-data")
include(":features:movies:movies-listing")
include(":features:movies:movies-detail")
