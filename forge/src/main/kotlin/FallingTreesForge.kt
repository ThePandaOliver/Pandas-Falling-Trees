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
package dev.pandasystems.fallingtrees.forge

import dev.pandasystems.fallingtrees.FallingTrees
import dev.pandasystems.fallingtrees.forge.client.FallingTreesClientNeoForge
import dev.pandasystems.pandalib.utils.gameEnvironment
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext

@Mod(FallingTrees.modid)
class FallingTreesForge {
	init {
		val eventBus = FMLJavaModLoadingContext.get().modEventBus
		FallingTrees

		if (gameEnvironment.isClient) {
			FallingTreesClientNeoForge(eventBus)
		}
	}
}
