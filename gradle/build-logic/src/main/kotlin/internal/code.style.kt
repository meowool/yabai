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

import com.meowool.cradle.util.isCiEnvironment
import de.undercouch.gradle.tasks.download.Download
import de.undercouch.gradle.tasks.download.DownloadTaskPlugin
import io.github.detekt.gradle.DetektKotlinCompilerPlugin
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.withType

private const val DownloadPluginJarTask = "copyDetektPlugins"

internal fun Project.lintCodeStyle() {
  apply<DetektPlugin>()
  apply<DownloadTaskPlugin>()
  addDetektPlugin(libs.detekt.formatting)
  extensions.configure<DetektExtension> {
    parallel = !isCiEnvironment
    baseline = miscFile("detekt/baseline.xml")
    config.from(miscFile("detekt/config.yml"))
  }
  tasks.register<Download>(DownloadPluginJarTask) {
    dest(miscFile("detekt/plugins"))
  }
}

internal fun Project.addDetektPlugin(dependency: Any) =
  plugins.withType<DetektPlugin> { dependencies.add("detektPlugins", dependency) }


internal fun Project.addDetektPluginJar(url: String) =
  tasks.named<Download>(DownloadPluginJarTask) { src(arrayOf(url, src)) }

internal fun Project.configureDetektSource(path: Any) =
  extensions.configure<DetektExtension> { source.from(path) }
