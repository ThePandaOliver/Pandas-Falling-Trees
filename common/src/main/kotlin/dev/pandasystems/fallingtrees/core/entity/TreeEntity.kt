package dev.pandasystems.fallingtrees.core.entity

import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class TreeEntity(type: EntityType<*>, level: Level) : Entity(type, level) {
	override fun defineSynchedData(entityData: SynchedEntityData.Builder) {
		TODO("Not yet implemented")
	}

	override fun hurtServer(
		level: ServerLevel,
		source: DamageSource,
		damage: Float
	): Boolean {
		TODO("Not yet implemented")
	}

	override fun readAdditionalSaveData(input: ValueInput) {
		TODO("Not yet implemented")
	}

	override fun addAdditionalSaveData(output: ValueOutput) {
		TODO("Not yet implemented")
	}
}