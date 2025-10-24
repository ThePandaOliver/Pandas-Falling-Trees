/*
 * Copyright (C) 2024 Oliver Froberg (The Panda Oliver)
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
import dev.pandasystems.pandalib.config.ConfigCategory

class TreeConfigs {
	@ConfigCategory
	val genericTree = GenericTreeConfig()
	@ConfigCategory
	val verticalTree = VerticalTreeConfig()
	@ConfigCategory
	val chorusTree = TreeConfig()
	@ConfigCategory
	val mushroomTree = TreeConfig()
}
