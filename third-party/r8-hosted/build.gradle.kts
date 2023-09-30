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

@file:Suppress("SpellCheckingInspection")

plugins {
  id(kotlinJvm)
}

dependencies {
  // The following dependencies are required by the Google R8 project:
  implementationOf(
    libs.bundles.asm,
    libs.google.gson,
    libs.google.guava,
    libs.kotlin.metadata,
    libs.fastutil config {
      // As of now, we strictly follow the fastutil version used by R8,
      // because R8's code is not compatible with the new version
      version { strictly("7.2.1") }
    },
  )
}

sourceSets.main {
  java {
    srcDirs("keepanno/java")
    // Exclude R8's old setup
    // https://r8.googlesource.com/r8/+/refs/heads/main/build.gradle#55
    exclude("com/android/tools/r8/utils/resourceshrinker/*.java")
  }
}
