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

import dev.pandasystems.fallingtrees.FallingTrees
import dev.pandasystems.pandalib.config.ConfigRegistry
import dev.pandasystems.pandalib.config.syncOption

val fallingTreesClientConfig = ConfigRegistry.create(FallingTrees.resourceLocation("client"), ClientConfig)
val fallingTreesCommonConfig = ConfigRegistry.create(FallingTrees.resourceLocation("common"), CommonConfig)

internal fun initConfigs() {
	fallingTreesClientConfig.load()
	fallingTreesClientConfig.syncOption(ClientConfig::miningShould)
	fallingTreesClientConfig.syncOption(ClientConfig::miningWhileCrouchingShould)
	fallingTreesCommonConfig.load()
}