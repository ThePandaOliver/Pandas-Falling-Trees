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

package me.pandamods.fallingtrees;

import dev.architectury.platform.Platform;
import me.pandamods.fallingtrees.compat.Compat;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import me.pandamods.fallingtrees.event.EventHandler;
import me.pandamods.fallingtrees.registry.EntityRegistry;
import me.pandamods.fallingtrees.registry.SoundRegistry;
import me.pandamods.fallingtrees.registry.TreeRegistry;
import me.pandamods.fallingtrees.utils.BlockMapEntityData;
import me.pandamods.fallingtrees.utils.ItemListEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.resources.ResourceLocation;

public class FallingTrees {
    public static final String MOD_ID = "fallingtrees";
	private static FallingTrees instance;

	public static final FallingTreesConfig CONFIG = new FallingTreesConfig();
	private static Compat compat;

    public FallingTrees(Compat compat) {
		FallingTrees.compat = compat;

		TreeRegistry.TREES.register();
		SoundRegistry.SOUNDS.register();
		EntityRegistry.ENTITIES.register();
		EventHandler.register();

		if (!Platform.isNeoForge()) {
			EntityDataSerializers.registerSerializer(BlockMapEntityData.BLOCK_MAP);
			EntityDataSerializers.registerSerializer(ItemListEntityData.ITEM_LIST);
		}
		instance = this;
    }

	public static ResourceLocation resourceLocation(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	public static Compat getCompat() {
		if (compat == null)
			throw new NullPointerException("Panda's Falling Tree's mod compat class not initialized.");
		return compat;
	}

	public static FallingTrees getInstance() {
		return instance;
	}
}
