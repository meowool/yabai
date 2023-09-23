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

import com.android.build.api.dsl.LibraryExtension
import internal.enableCompose
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure

apply(plugin = androidLibrary)

extensions.configure<LibraryExtension> { enableCompose(project) }
