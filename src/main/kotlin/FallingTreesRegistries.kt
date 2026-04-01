/*
 * Copyright (C) 2025-2025 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.pandasystems.fallingtrees

import com.mojang.serialization.Lifecycle
import dev.pandasystems.fallingtrees.FallingTrees.resourceLocation
import dev.pandasystems.fallingtrees.api.TreeType
import dev.pandasystems.fallingtrees.entity.TreeEntity
import dev.pandasystems.fallingtrees.trees.ChorusTree
import dev.pandasystems.fallingtrees.trees.GenericTree
import dev.pandasystems.fallingtrees.trees.VerticalTree
import dev.pandasystems.fallingtrees.trees.mushroom.MushroomTree
import dev.pandasystems.fallingtrees.utils.BlockMapEntityData
import dev.pandasystems.fallingtrees.utils.ItemListEntityData
import dev.pandasystems.pandalib.registry.ENTITY_DATA_SERIALIZERS_REGISTRY
import dev.pandasystems.pandalib.registry.deferred.DeferredRegister
import dev.pandasystems.pandalib.utils.resourceLocation
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.block.state.BlockState

// Registries

val treeRegistryKey: ResourceKey<Registry<TreeType>> = ResourceKey.createRegistryKey(FallingTrees.resourceLocation("tree_registry"))
val treeRegistry = DeferredRegister.registerNewRegistry(MappedRegistry(treeRegistryKey, Lifecycle.stable()))


// Deferred registers

val treeRegister = DeferredRegister.create(FallingTrees.MODID, treeRegistryKey)
val soundRegister = DeferredRegister.create(FallingTrees.MODID, Registries.SOUND_EVENT)
val entityRegister = DeferredRegister.create(FallingTrees.MODID, Registries.ENTITY_TYPE)
val entityDataRegister = DeferredRegister.create(FallingTrees.MODID, ENTITY_DATA_SERIALIZERS_REGISTRY)


// Trees

var genericTreeType = treeRegister.register("generic") { GenericTree() }
var verticalTreeType = treeRegister.register("vertical") { VerticalTree() }
var chorusTreeType = treeRegister.register("chorus") { ChorusTree() }
var mushroomTreeType = treeRegister.register("mushroom") { MushroomTree() }

fun getTree(blockState: BlockState): TreeType? {
	for (tree in treeRegistry) {
		if (tree.isTreeStem(blockState)) return tree
	}
	return null
}

fun getTree(identifier: ResourceLocation): TreeType? {
	return treeRegistry.getValue(identifier)
}

fun getTreeLocation(tree: TreeType): ResourceLocation {
	return treeRegistry.getKey(tree)!!
}


// Sound

val treeFallSound = soundRegister.register("tree_fall") { createFixedRangeEvent(FallingTrees.resourceLocation("tree_fall"), 16) }
val treeImpactSound = soundRegister.register("tree_impact") { createFixedRangeEvent(FallingTrees.resourceLocation("tree_impact"), 16) }

private fun createFixedRangeEvent(identifier: ResourceLocation, range: Int): SoundEvent {
	return SoundEvent.createFixedRangeEvent(identifier, range.toFloat())
}


// Entity

val treeEntity = entityRegister.register("tree") {
	EntityType.Builder
		.of(EntityType.EntityFactory(::TreeEntity), MobCategory.MISC)
		.sized(0.5f, 0.5f)
		.noSave()
		.fireImmune()
		.build(it)
}


// Entity Data

val blockMapSerializer = entityDataRegister.register("block_map") { BlockMapEntityData }
val itemListSerializer = entityDataRegister.register("item_list") { ItemListEntityData }