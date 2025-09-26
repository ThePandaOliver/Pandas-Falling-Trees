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
import dev.pandasystems.fallingtrees.config.client.AnimationConfig
import dev.pandasystems.fallingtrees.config.client.SoundSettingsConfig
import dev.pandasystems.pandalib.impl.config.Configuration

@Configuration(modId = FallingTrees.MOD_ID, pathName = FallingTrees.MOD_ID + "_client")
class ClientConfig {
	var miningShould = MiningOptionEnum.CHOP_TREE
	var miningWhileCrouchingShould = MiningOptionEnum.MINE_SINGLE_BLOCK
	var soundSettings = SoundSettingsConfig()
	var animation = AnimationConfig()
}

enum class MiningOptionEnum {
	CHOP_TREE,
	MINE_SINGLE_BLOCK
}
