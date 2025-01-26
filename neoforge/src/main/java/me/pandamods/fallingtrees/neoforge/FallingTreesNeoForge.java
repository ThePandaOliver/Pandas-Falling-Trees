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

package me.pandamods.fallingtrees.neoforge;

import me.pandamods.fallingtrees.FallingTrees;
import me.pandamods.fallingtrees.utils.BlockMapEntityData;
import me.pandamods.fallingtrees.utils.ItemListEntityData;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(FallingTrees.MOD_ID)
public class FallingTreesNeoForge {
	public final static DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA =
			DeferredRegister.create(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, FallingTrees.MOD_ID);

    public FallingTreesNeoForge(IEventBus eventBus) {
		FallingTrees.init(null);
		ENTITY_DATA.register("block_map", () -> BlockMapEntityData.BLOCK_MAP);
		ENTITY_DATA.register("item_list", () -> ItemListEntityData.ITEM_LIST);
		ENTITY_DATA.register(eventBus);
	}
}
