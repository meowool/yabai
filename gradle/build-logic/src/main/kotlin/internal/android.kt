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

package internal

import com.android.build.api.dsl.ApplicationDefaultConfig
import com.android.build.api.dsl.CommonExtension
import com.meowool.cradle.util.isCiEnvironment
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.get
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

internal typealias BaseExtension = CommonExtension<*, *, *, *, *>

@PublishedApi
internal inline fun Project.androidBaseExtension(block: BaseExtension.() -> Unit = {}) =
  (extensions["android"] as BaseExtension).apply(block)

internal fun BaseExtension.init(project: Project) = with(project) {
  namespace = buildString {
    append(rootProject.group)
    // :module:feature:left-right -> .module.feature.left.right
    append(
      project.path
        .replace("-", ".").split(":").drop(2)
        .joinToString(separator = ".", prefix = ".")
    )
  }
  compileSdk = 34
  defaultConfig {
    if (this is ApplicationDefaultConfig) {
      // We set the target sdk to the same as to compile sdk, so that we can be compatible
      // with new Android features. This is because we always choose higher Android APIs as
      // the basis for development, which helps to improve the experience of high-version
      // Android users.
      targetSdk = compileSdk
    }
    minSdk = when {
      // We choose to distribute APKs with different min-sdk versions through CI,
      // and use desugar-tools to ensure code compatibility for lower versions.
      isCiEnvironment -> requireNotNull(providers.environmentVariable("ANDROID_MIN_SDK").orNull) {
        "The env `ANDROID_MIN_SDK` is required when running on CI."
      }.toInt().also {
        // Desugar is only required if the app's min-sdk is less than 26:
        // https://developer.android.com/studio/write/java8-support#supported_features
        if (it < 26) {
          compileOptions.isCoreLibraryDesugaringEnabled = true
          dependencies.add("coreLibraryDesugaring", libs.android.desugar)
        }
      }
      // We default to raising the minimum version to Android Oreo (8.0).
      else -> 26
    }
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  compileOptions {
    sourceCompatibility(JavaToolchainVersion)
    targetCompatibility(JavaToolchainVersion)
  }
  packaging.resources {
    excludes += "META-INF/LICENSE*"
  }
  dependencies {
    "implementation"(libs.androidx.core)
    // https://developer.android.com/jetpack/androidx/releases/test#declaring_dependencies
    "androidTestImplementation"(libs.androidx.test.core)
    "androidTestImplementation"(libs.androidx.test.runner)
    "androidTestImplementation"(libs.androidx.test.rules)
    "androidTestImplementation"(libs.androidx.test.ext.junit)
  }
}

internal fun BaseExtension.enableCompose(project: Project) = with(project) {
  val compose = libs.androidx.compose
  val composeBom = compose.bom.get()

  buildFeatures.compose = true
  composeOptions.kotlinCompilerExtensionVersion = compose.compiler.orNull?.version
    ?: error("Compose compiler version not found.")

  dependencies {
    "implementation"(platform(composeBom))
    "implementation"(libs.bundles.androidx.compose)

    // We use Detekt to detect the code style of Jetpack-Compose
    addDetektPlugin(libs.detekt.compose)
  }

  // Configure the Compose compiler reports
  // https://chrisbanes.me/posts/composable-metrics/
  // https://github.com/android/nowinandroid/blob/main/README.md#compose-compiler-metrics
  tasks.create("generateComposeCompilerReports") {
    dependsOn("assembleRelease")
    doFirst {
      configureKotlinCompile {
        val output = rootProject.layout.buildDirectory.get().dir("compose-reports").asFile
        composePluginArgs("metricsDestination" to output, "reportsDestination" to output)
      }
    }
  }
}

internal fun KotlinCompile<*>.composePluginArgs(vararg args: Pair<String, Any>) =
  pluginArgs(pluginId = "androidx.compose.compiler.plugins.kotlin", args = args)
