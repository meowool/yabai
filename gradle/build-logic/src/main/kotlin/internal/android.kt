package internal

import com.android.build.api.dsl.ApplicationDefaultConfig
import com.android.build.api.dsl.CommonExtension
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
    minSdk = 21
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  compileOptions {
    // We enable desugaring to ensure code compatibility for lower versions:
    // https://developer.android.com/studio/write/java8-support#supported_features
    isCoreLibraryDesugaringEnabled = true

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

    "coreLibraryDesugaring"(libs.android.desugar)
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
