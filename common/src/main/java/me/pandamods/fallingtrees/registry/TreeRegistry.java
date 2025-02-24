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

package me.pandamods.fallingtrees.registry;

import com.mojang.serialization.Lifecycle;
import me.pandamods.fallingtrees.FallingTrees;
import me.pandamods.fallingtrees.api.TreeType;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import me.pandamods.fallingtrees.config.common.TreeConfigs;
import me.pandamods.fallingtrees.trees.ChorusTree;
import me.pandamods.fallingtrees.trees.GenericTree;
import me.pandamods.fallingtrees.trees.mushroom.MushroomTree;
import me.pandamods.fallingtrees.trees.VerticalTree;
import me.pandamods.pandalib.registry.DeferredObject;
import me.pandamods.pandalib.registry.DeferredRegister;
import me.pandamods.pandalib.registry.RegistryRegister;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public class TreeRegistry {
	public static final ResourceKey<Registry<TreeType>> TREE_REGISTRY_KEY = ResourceKey.createRegistryKey(FallingTrees.resourceLocation("tree_registry"));
	public static final Registry<TreeType> TREE_REGISTRY = RegistryRegister.register(new MappedRegistry<>(TREE_REGISTRY_KEY, Lifecycle.stable(), null));
	
	public static final DeferredRegister<TreeType> TREES = DeferredRegister.create(FallingTrees.MOD_ID, TREE_REGISTRY_KEY);
	
	public static DeferredObject<GenericTree> GENERIC;
	public static DeferredObject<VerticalTree> VERTICAL;
	public static DeferredObject<ChorusTree> CHORUS;
	public static DeferredObject<MushroomTree> MUSHROOM;
	
	static {
		TreeConfigs treeConfigs = FallingTreesConfig.getCommonConfig().trees;
		
		if (treeConfigs.genericTree.enabled)
			GENERIC = TREES.register("generic", GenericTree::new);

		if (treeConfigs.verticalTree.enabled)
			VERTICAL = TREES.register("vertical", VerticalTree::new);

		if (treeConfigs.chorusTree.enabled)
			CHORUS = TREES.register("chorus", ChorusTree::new);

		if (treeConfigs.mushroomTree.enabled)
			MUSHROOM = TREES.register("mushroom", MushroomTree::new);
	}
	
	public static TreeType getTree(BlockState blockState) {
		for (TreeType tree : TREE_REGISTRY) {
			if (tree.isTreeStem(blockState))
				return tree;
		}
		return null;
	}
	
	public static TreeType getTree(ResourceLocation resourceLocation) {
		return TREE_REGISTRY.get(resourceLocation);
	}
	
	public static ResourceLocation getTreeLocation(TreeType tree) {
		return TREE_REGISTRY.getKey(tree);
	}
}
