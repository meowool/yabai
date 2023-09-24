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
import io.gitlab.arturbosch.detekt.DetektPlugin
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.KotlinBasePlugin

internal fun Project.lintCodeStyle() = plugins.withType<KotlinBasePlugin> {
  apply<DetektPlugin>()
  addDetektPlugin(libs.detekt.formatting)

  extensions.configure<DetektExtension> {
    buildUponDefaultConfig = true
    parallel = !isCiEnvironment
    baseline = miscFile("detekt/baseline.xml")
    config.from(miscFile("detekt/config.yml"))
  }
}

internal fun Project.addDetektPlugin(dependency: Any) =
  plugins.withType<DetektPlugin> { dependencies.add("detektPlugins", dependency) }

internal fun Project.configureDetektSource(path: Any) =
  plugins.withType<DetektPlugin> {
    extensions.configure<DetektExtension> { source.from(path) }
  }
