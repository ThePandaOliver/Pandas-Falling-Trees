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

import dev.pandasystems.fallingtrees.config.common.TreeConfigs
import dev.pandasystems.pandalib.config.Config
import dev.pandasystems.pandalib.config.ConfigCategory
import dev.pandasystems.pandalib.config.options.configOption

class CommonConfig : Config() {
	val disableCrouchMining by configOption(false)
	val disableExtraToolDamage by configOption(false)
	val disableExtraFoodExhaustion by configOption(false)

	val treeLifetimeLength by configOption(4f)

	@ConfigCategory
	val dynamicMiningSpeed: DynamicMiningSpeed = DynamicMiningSpeed()
	@ConfigCategory
	val trees: TreeConfigs = TreeConfigs()

	class DynamicMiningSpeed {
		val disable by configOption(false)
		val speedMultiplication by configOption( 0.5f)
		val maxSpeedMultiplication by configOption(16f)
	}
}
