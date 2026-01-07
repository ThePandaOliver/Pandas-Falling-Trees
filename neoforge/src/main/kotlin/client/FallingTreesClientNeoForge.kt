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
package dev.pandasystems.fallingtrees.neoforge.client

import dev.pandasystems.fallingtrees.FallingTrees
import dev.pandasystems.fallingtrees.client.FallingTreesClient
import net.neoforged.api.distmarker.Dist
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod

@Mod(value = FallingTrees.MODID, dist = [Dist.CLIENT])
class FallingTreesClientNeoForge(modBus: IEventBus) {
	init {
		FallingTreesClient
	}
}