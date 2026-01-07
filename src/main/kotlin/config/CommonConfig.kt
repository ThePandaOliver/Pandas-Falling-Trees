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
package dev.pandasystems.fallingtrees.config

import dev.pandasystems.fallingtrees.config.common.TreeConfigs

object CommonConfig {
	var disableCrouchMining = false
	var disableExtraToolDamage = false
	var disableExtraFoodExhaustion = false

	var treeLifetimeLength = 4f

	val dynamicMiningSpeed: DynamicMiningSpeed = DynamicMiningSpeed()
	val trees: TreeConfigs = TreeConfigs()

	class DynamicMiningSpeed {
		var disable = false
		var speedMultiplication = 0.5f
		var maxSpeedMultiplication = 16f
	}
}
