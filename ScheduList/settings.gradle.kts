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

rootProject.name = "ScheduList"
include(":app")

// Note: Ensure a Gradle wrapper (gradlew) exists at repo root or in ScheduList/ so the workflow can run.
// Local: run `./gradlew dokkaHtml` (or `./ScheduList/gradlew dokkaHtml`) and verify output under <module>/build/dokka/html
// How to run the workflow manually:
// - GitHub UI: Actions → Generate and publish Dokka docs → Run workflow → set input 'debug' = 'true' (optional)
// - gh CLI: gh workflow run dokka.yml --ref main -f debug=true
// Local quick test:
//   ./gradlew dokkaHtml
// or if wrapper is in ScheduList:
//   ./ScheduList/gradlew dokkaHtml
