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
        maven("https://github.com/psiegman/mvn-repo/raw/master/releases")
    }
}

rootProject.name = "AvitoTask"
include(":app")
include(":feature")
include(":data")
include(":core")
include(":data:firebaseauth")
include(":core:util")
include(":feature:auth")
include(":data:firebasefirestore")
include(":data:firebasestorage")
include(":domain")
include(":domain:books")
include(":feature:uploadbooks")
include(":domain:auth")
include(":core:firebasefirestore")
include(":core:yandexcloud")
include(":data:bookslist")
include(":feature:listofbooks")
include(":domain:bookslist")
include(":core:firebaseauth")
