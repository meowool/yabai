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

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
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

val isCiEnvironment: Boolean get() = System.getenv("CI") != null

internal fun RepositoryHandler.sonatypeSnapshots(
  includeS01: Boolean = true,
  includeOld: Boolean = true,
  action: MavenArtifactRepository.() -> Unit = {},
) {
  if (includeS01) maven {
    name = "Sonatype OSS S01 Snapshots"
    setUrl("https://s01.oss.sonatype.org/content/repositories/snapshots")
    mavenContent { snapshotsOnly() }
    action()
  }
  if (includeOld) maven {
    name = "Sonatype OSS Snapshots"
    setUrl("https://oss.sonatype.org/content/repositories/snapshots")
    mavenContent { snapshotsOnly() }
    action()
  }
}

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
  args = args.map { (key, value) -> listOf("-P", "plugin:$pluginId:$key=$value") }
    .flatten()
    .toTypedArray(),
)

internal fun Project.miscFile(path: String) =
  rootProject.layout.projectDirectory.file("misc/$path").asFile
