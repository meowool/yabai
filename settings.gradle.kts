enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
  includeBuild("gradle/build-logic")
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
  }
}

includeAllProjects()

rootProject.name = "yabai"
