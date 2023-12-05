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

plugins { `kotlin-dsl` }

dependencies {
  arrayOf(
    libs.gradle.kotlin.dsl,
    libs.kotlin.gradle.plugin,
    libs.android.gradle.plugin,
    libs.google.hilt.gradle.plugin,
    libs.google.ksp.gradle.plugin,
    libs.detekt.gradle.plugin,

    platform(libs.kotlin.bom),
    // A workaround to enable version catalog usage in the convention plugin,
    // see https://github.com/gradle/gradle/issues/15383#issuecomment-779893192
    files(libs.javaClass.superclass.protectionDomain.codeSource.location),
  ).forEach(::implementation)
}
