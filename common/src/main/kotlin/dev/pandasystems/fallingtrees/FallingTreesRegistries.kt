package dev.pandasystems.fallingtrees

import dev.pandasystems.fallingtrees.entity.TreeEntity
import dev.pandasystems.pandalib.core.handles.id
import dev.pandasystems.pandalib.registry.DeferredRegistry
import net.minecraft.core.registries.Registries
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory

object FallingTreesRegistries {
	// Deferred registers

	val soundRegister = DeferredRegistry.create(Registries.SOUND_EVENT)
	val entityRegister = DeferredRegistry.create(Registries.ENTITY_TYPE)

	// Sound

	val treeFallSound = soundRegister.register(id(modId, "tree_fall")) { SoundEvent.createVariableRangeEvent(it.identifier()) }
	val treeImpactSound = soundRegister.register(id(modId, "tree_impact")) { SoundEvent.createVariableRangeEvent(it.identifier()) }

	// Entity

	val treeEntity = entityRegister.register(id(modId, "tree")) {
		EntityType.Builder
			.of(EntityType.EntityFactory(::TreeEntity), MobCategory.MISC)
			.sized(0.5f, 0.5f)
			.noSave()
			.fireImmune()
			.build(it)
	}

	fun register() {
		soundRegister.finalizeRegistration()
		entityRegister.finalizeRegistration()
	}
}
