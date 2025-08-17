/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU General Public License v3.0
 * See: https://www.gnu.org/licenses/gpl-3.0-standalone.html
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
import dev.pandasystems.pandalib.api.registry.RegistryRegister
import dev.pandasystems.pandalib.api.registry.deferred.DeferredRegister
import dev.pandasystems.pandalib.core.platform.registry.registryRegistrations
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.world.level.block.state.BlockState
import java.util.function.Supplier

// Registries

val treeRegistryKey: ResourceKey<Registry<TreeType>> = ResourceKey.createRegistryKey(resourceLocation("tree_registry"))
val treeRegistry = RegistryRegister.register(MappedRegistry(treeRegistryKey, Lifecycle.stable()))


// Deferred registers

val treeRegister = DeferredRegister.create(FallingTrees.MOD_ID, treeRegistryKey)
val soundRegister = DeferredRegister.create(FallingTrees.MOD_ID, Registries.SOUND_EVENT)
val entityRegistar = DeferredRegister.create(FallingTrees.MOD_ID, Registries.ENTITY_TYPE)
val entityDataRegistar = DeferredRegister.create(FallingTrees.MOD_ID, registryRegistrations.entityDataSerializers)


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

fun getTree(resourceLocation: ResourceLocation): TreeType? {
	return treeRegistry.getValue(resourceLocation)
}

fun getTreeLocation(tree: TreeType): ResourceLocation {
	return treeRegistry.getKey(tree)!!
}


// Sound

val treeFallSound = soundRegister.register("tree_fall") { createFixedRangeEvent(resourceLocation("tree_fall"), 16) }
val treeImpactSound = soundRegister.register("tree_impact") { createFixedRangeEvent(resourceLocation("tree_impact"), 16) }

private fun createFixedRangeEvent(resourceLocation: ResourceLocation, range: Int): SoundEvent {
	return SoundEvent.createFixedRangeEvent(resourceLocation, range.toFloat())
}


// Entity

val treeEntity = entityRegistar.register("tree") {
	EntityType.Builder
		.of(EntityType.EntityFactory(::TreeEntity), MobCategory.MISC)
		.sized(0.5f, 0.5f)
		.noSave()
		.fireImmune()
		.build(it)
}


// Entity Data

val blockMapSerializer = entityDataRegistar.register("block_map") { BlockMapEntityData.BLOCK_MAP }
val itemListSerializer = entityDataRegistar.register("item_list") { ItemListEntityData.ITEM_LIST }