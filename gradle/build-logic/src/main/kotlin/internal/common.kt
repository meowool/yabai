package internal

import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin

/**
 * We are using this constant to unify the Java toolchain version for the project,
 * as the source of truth.
 */
internal const val JavaToolchainVersion = 17

internal fun Project.configureJvmToolchain() {
  plugins.withType<JavaBasePlugin> {
    configure<JavaPluginExtension> {
      toolchain.languageVersion.set(JavaLanguageVersion.of(JavaToolchainVersion))
    }
  }
  plugins.withType<KotlinBasePlugin> {
    configure<KotlinProjectExtension> {
      jvmToolchain(JavaToolchainVersion)
    }
  }
}

internal fun Project.configureKotlin(block: KotlinProjectExtension.() -> Unit) {
  plugins.withType<KotlinBasePlugin> { extensions.configure(block) }
}

internal fun Project.configureKotlinCompile(configuration: KotlinCompile<*>.() -> Unit) =
  configureKotlin { tasks.withType(configuration) }

internal fun KotlinCompile<*>.args(vararg args: String) = kotlinOptions {
  freeCompilerArgs = (freeCompilerArgs + args).distinct()
}

internal fun KotlinCompile<*>.pluginArgs(pluginId: String, vararg args: Pair<String, Any>) = args(
  *args.map { (key, value) -> listOf("-P", "plugin:$pluginId:$key=$value") }
    .flatten()
    .toTypedArray()
)
