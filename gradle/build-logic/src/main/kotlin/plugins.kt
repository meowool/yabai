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

// The meaning of this file is very simple, just to map the names of all
// precompiled plugins in build-logic.
val kotlinJvm inline get() = "kotlin-jvm"
val androidLibrary inline get() = "android-library"
val androidApplication inline get() = "android-application"
val androidComposeLibrary inline get() = "android-compose-library"
val androidComposeApplication inline get() = "android-compose-application"
val ksp inline get() = "ksp"
val kspLibrary inline get() = "ksp-library"
