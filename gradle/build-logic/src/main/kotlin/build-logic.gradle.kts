/*
 * Copyright (C) 2023 Meowool <https://github.com/meowool/graphs/contributors>
 *
 * This file is part of the Yabai project <https://github.com/meowool/yabai>.
 *
 * Yabai is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * Yabai is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Yabai.  If not, see <https://www.gnu.org/licenses/>.
 */

import com.android.build.api.dsl.AndroidSourceSet
import com.android.build.gradle.BasePlugin
import internal.androidBaseExtension
import internal.args
import internal.configureJvmToolchain
import internal.configureKotlinCompile
import internal.libs
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

allprojects {
  group = "com.chachako.yabai"

  repositories {
    google { filterAndroidDependencies(include = true) }
    mavenCentral { filterAndroidDependencies(include = false) }
    sonatypeSnapshots()
  }

  configureCompile()
  configureSourceSets()
  configureBuildDirectory()

  afterEvaluate {
    alignVersions()
  }
}

/**
 * Cleaning the project.
 * We can delete all empty directories when running the root `clean` task,
 * and we have no concerns about this.
 */
tasks.register<Delete>("clean") {
  delete = projectDir.walk().filter {
    it.isDirectory &&
      it.name != ".git" &&
      it.name != ".idea" &&
      it.name != ".gradle" &&
      it.name != ".build" &&
      it.listFiles()?.isEmpty() == true
  }.toSet()
}

/**
 * We redirect the build directory of all projects to the `.build` directory of the
 * root project. This directory structure is more readable for the monorepo.
 *
 * And it is also better for "git" management as we only need to ignore it
 * in the `.gitignore` file of the root project.
 *
 * ```
 * ├─ .build
 * │  ├─ Sub Project folder 1
 * │  │  ├─ ...
 * │  │  └─ ...
 * │  │     └─ ...
 * │  ├─ Sub Project folder 2
 * │  │  ├─ ...
 * │  │  └─ ...
 * │  ├─ ......
 * │  └─ * Root Project build files
 * ```
 */
fun Project.configureBuildDirectory() = layout.buildDirectory.set(
  when (this) {
    rootProject -> file(".build")
    else -> rootProject.layout.buildDirectory.get().file(path.drop(1).replace(':', '/')).asFile
  }
)

/**
 * We can skip the creation of the "src/" directory. Instead, this allows us to create
 * its subdirectories directly.
 */
fun Project.configureSourceSets() {
  // For Kotlin projects
  plugins.withType<KotlinBasePlugin> {
    extensions.configure<KotlinProjectExtension> {
      sourceSets.configureEach {
        kotlin.srcDir("$name/kotlin")
        resources.srcDir("$name/resources")
        if (name == "main") kotlin.srcDir("kotlin")
      }
    }
    if (this !is KotlinMultiplatformPluginWrapper) dependencies {
      "implementation"(platform(libs.kotlin.bom))
    }
  }
  // For Android projects
  plugins.withType<BasePlugin> {
    androidBaseExtension {
      sourceSets.configureEach {
        fun AndroidSourceSet.configure(name: String) {
          res.srcDir("$name/res")
          java.srcDir("$name/java")
          jniLibs.srcDir("$name/jni")
          kotlin.srcDir("$name/kotlin")
          assets.srcDir("$name/assets")
          resources.srcDir("$name/resources")
          manifest.srcFile("$name/AndroidManifest.xml")
          baselineProfiles.srcDir("$name/baseline")
        }
        when (name) {
          "test" -> configure("unitTest")
          "androidTest" -> configure("instrumentTest")
          else -> {
            configure(name)
            if (name == "main") kotlin.srcDir("kotlin")
          }
        }
      }
    }
  }
  // For general JVM projects
  plugins.withType<JavaBasePlugin> {
    extensions.configure<SourceSetContainer> {
      configureEach {
        java.srcDir("$name/java")
        resources.srcDir("$name/resources")
        if (name == "main") java.srcDir("java")
      }
    }
  }
}

fun Project.configureCompile() {
  configureJvmToolchain()
  // Add experimental language features to Kotlin projects
  @Suppress("SpellCheckingInspection")
  configureKotlinCompile { args("-Xcontext-receivers") }
}

/**
 * Use [BOM](https://docs.gradle.org/current/userguide/platforms.html#sub:bom_import)
 * to align version of dependencies of the same series.
 */
fun Project.alignVersions() {
  // For Kotlin multiplatform projects
  plugins.withType<KotlinMultiplatformPluginWrapper> {
    dependencies {
      "commonMainImplementation"(platform(libs.kotlin.bom))
    }
  }
  // For general JVM projects
  plugins.withType<JavaBasePlugin> {
    dependencies {
      "implementation"(platform(libs.kotlin.bom))
    }
  }
}

/**
 * Filtering Android dependencies across different repositories can reduce network
 * requests and speed up Gradle build times. This is because we are aware that these
 * dependencies are only available in Google Maven repository.
 */
fun MavenArtifactRepository.filterAndroidDependencies(include: Boolean) = mavenContent {
  arrayOf(
    "androidx\\..*",
    "com\\.android.*",
    "com\\.google\\.testing\\..*",
  ).forEach { group ->
    if (include) includeGroupByRegex(group) else excludeGroupByRegex(group)
  }
}
