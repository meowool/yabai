dependencyResolutionManagement {
  repositories {
    google {
      content {
        // We limit the scope of this repository to reduce network requests and
        // speed up Gradle build times.
        includeGroupByRegex("androidx\\..*")
        includeGroupByRegex("com\\.android.*")
        includeGroupByRegex("com\\.google\\.testing\\..*")
      }
    }
    gradlePluginPortal()
    mavenCentral()
  }
  versionCatalogs {
    create("libs") {
      from(files("../libs.versions.toml"))
      library(
        "gradle-kotlin-dsl",
        "org.gradle.kotlin:gradle-kotlin-dsl-plugins:$expectedKotlinDslPluginsVersion"
      )
    }
  }
}

rootProject.name = "build-logic"
