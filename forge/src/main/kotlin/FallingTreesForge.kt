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
import dev.pandasystems.fallingtrees.utils.BlockMapEntityData
import dev.pandasystems.fallingtrees.utils.ItemListEntityData
import dev.pandasystems.pandalib.utils.gameEnvironment
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries

@Mod(FallingTrees.modid)
class FallingTreesForge(context: FMLJavaModLoadingContext) {
	init {
		val eventBus = context.modEventBus

		EntityDataSerializers.registerSerializer(BlockMapEntityData.BLOCK_MAP)
		EntityDataSerializers.registerSerializer(ItemListEntityData.ITEM_LIST)

		FallingTrees

		if (gameEnvironment.isClient) {
			FallingTreesClientNeoForge(eventBus)
		}
	}
}
