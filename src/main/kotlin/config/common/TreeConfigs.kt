/*
 * Copyright (C) 2025 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package dev.pandasystems.fallingtrees.config.common

import dev.pandasystems.fallingtrees.config.common.tree.GenericTreeConfig
import dev.pandasystems.fallingtrees.config.common.tree.TreeConfig
import dev.pandasystems.fallingtrees.config.common.tree.VerticalTreeConfig

class TreeConfigs {
	val genericTree = GenericTreeConfig()
	val verticalTree = VerticalTreeConfig()
	val chorusTree = TreeConfig()
	val mushroomTree = TreeConfig()
}
