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

import dev.pandasystems.fallingtrees.config.client.AnimationConfig
import dev.pandasystems.fallingtrees.config.client.SoundSettingsConfig

object ClientConfig {
	var miningShould = MiningOptionEnum.CHOP_TREE
	var miningWhileCrouchingShould = MiningOptionEnum.MINE_SINGLE_BLOCK

	val soundSettings = SoundSettingsConfig()
	val animation = AnimationConfig()
}

enum class MiningOptionEnum {
	CHOP_TREE,
	MINE_SINGLE_BLOCK
}
