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

import com.android.build.gradle.BasePlugin
import internal.androidBaseExtension
import internal.args
import internal.configureDetektSource
import internal.configureJvmToolchain
import internal.configureKotlinCompile
import internal.libs
import internal.lintCodeStyle
import internal.sonatypeSnapshots
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper

allprojects {
  group = "com.meowool.yabai"

  repositories {
    google { filterAndroidDependencies(include = true) }
    mavenCentral { filterAndroidDependencies(include = false) }
    sonatypeSnapshots()
  }

  lintCodeStyle()
  configureCompile()
  configureSourceSets()
  configureBuildDirectory()
  configureCleanTask()

  afterEvaluate {
    // https://ci.chromium.org/ui/p/r8/builders/ci/archive/
    // Delay configuration, so that we can avoid errors caused by `libs` being loaded
    // at the root module.
    repositories.maven("https://storage.googleapis.com/r8-releases/raw/main") {
      content {
        with(libs.android.r8.get().module) {
          includeModule(group, name)
        }
      }
    }
    alignVersions()
  }
}

/**
 * Cleaning the project.
 * We can delete all empty directories when running the root `clean` task,
 * and we have no concerns about this.
 */
fun configureCleanTask() {
  tasks.maybeCreate<Delete>("clean").apply {
    delete = projectDir.walk().filter {
      it.isDirectory &&
        it.name != ".git" &&
        it.name != ".idea" &&
        it.name != ".gradle" &&
        it.name != ".build" &&
        it.listFiles()?.isEmpty() == true
    }.toSet()
  }
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
        resources.srcDir("$name/resources")
        kotlin.srcDir("$name/kotlin")
        project.configureDetektSource("$name/kotlin")
      }
    }
  }
  // For Android projects
  plugins.withType<BasePlugin> {
    androidBaseExtension {
      sourceSets.configureEach {
        fun configure(name: String) {
          res.srcDir("$name/res")
          java.srcDir("$name/java")
          jniLibs.srcDir("$name/jni")
          assets.srcDir("$name/assets")
          resources.srcDir("$name/resources")
          manifest.srcFile("$name/AndroidManifest.xml")
          baselineProfiles.srcDir("$name/baseline")

          kotlin.srcDir("$name/kotlin")
          project.configureDetektSource("$name/kotlin")
        }
        when (name) {
          "test" -> configure("unitTest")
          "androidTest" -> configure("instrumentTest")
          else -> configure(name)
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
      }
    }
  }
}

fun Project.configureCompile() {
  configureJvmToolchain()
  // Add experimental language features to Kotlin projects
  configureKotlinCompile { args("-Xcontext-receivers") }
}

/**
 * Use [BOM](https://docs.gradle.org/current/userguide/platforms.html#sub:bom_import)
 * to align version of dependencies of the same series.
 */
fun Project.alignVersions() {
  // For Kotlin multiplatform projects
  plugins.withType<KotlinMultiplatformPluginWrapper> {
    dependencies { "commonMainImplementation"(platform(libs.kotlin.bom)) }
  }
  // For general JVM projects
  plugins.withType<JavaBasePlugin> {
    dependencies { "implementation"(platform(libs.kotlin.bom)) }
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
