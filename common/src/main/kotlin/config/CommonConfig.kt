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
package dev.pandasystems.fallingtrees.config

import dev.pandasystems.fallingtrees.FallingTrees
import dev.pandasystems.fallingtrees.config.common.TreeConfigs
import dev.pandasystems.pandalib.impl.config.Configuration

@Configuration(modId = FallingTrees.MOD_ID, pathName = FallingTrees.MOD_ID + "_common")
class CommonConfig {
	var disableCrouchMining: Boolean = false
	var disableExtraToolDamage: Boolean = false
	var disableExtraFoodExhaustion: Boolean = false

	var treeLifetimeLength: Float = 4f

	var dynamicMiningSpeed: DynamicMiningSpeed = DynamicMiningSpeed()
	var trees: TreeConfigs = TreeConfigs()

	class DynamicMiningSpeed {
		var disable: Boolean = false
		var speedMultiplication: Float = 0.5f
		var maxSpeedMultiplication: Float = 16f
	}
}
