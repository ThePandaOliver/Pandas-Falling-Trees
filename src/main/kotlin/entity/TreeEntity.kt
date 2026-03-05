/*
 * Copyright (C) 2026 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package dev.pandasystems.fallingtrees.entity

import dev.pandasystems.fallingtrees.api.TreeBlob
import dev.pandasystems.fallingtrees.api.TreeType
import dev.pandasystems.fallingtrees.config.fallingTreesCommonConfig
import dev.pandasystems.fallingtrees.treeEntity
import dev.pandasystems.fallingtrees.treeRegistry
import dev.pandasystems.fallingtrees.utils.BlockMapEntityData
import dev.pandasystems.fallingtrees.utils.ItemListEntityData
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.joml.Math

class TreeEntity(entityType: EntityType<*> = treeEntity.get(), level: Level) : Entity(entityType, level) {
	lateinit var treeType: TreeType
	lateinit var owningPlayer: Player

	fun setData(treeBlob: TreeBlob, owningPlayer: Player) {
		this.treeType = treeBlob.treeType
		this.owningPlayer = owningPlayer
		val blockPoses = treeBlob.blockPoses
		val originBlock = treeBlob.originBlockPos
		val level = treeBlob.level

		val treeTypeLocation = treeRegistry.getKey(treeBlob.treeType)
		this.getEntityData().set(TREE_TYPE_LOCATION, treeTypeLocation.toString())

		val blockPosMap = mutableMapOf<BlockPos, BlockState>()
		for (pos in blockPoses) {
			blockPosMap[pos.immutable().subtract(originBlock)] = level.getBlockState(pos)
		}

		this.getEntityData().set(ORIGIN_POS, originBlock)
		this.getEntityData().set(BLOCKS, blockPosMap)

		this.getEntityData().set(
			FALL_DIRECTION, Direction.fromYRot(
				-Math.toDegrees(Math.atan2(owningPlayer.x - originBlock.x, owningPlayer.z - originBlock.z))
			).opposite
		)
	}

	override fun defineSynchedData(builder: SynchedEntityData.Builder) {
		builder.define(BLOCKS, mutableMapOf())
		builder.define(ORIGIN_POS, BlockPos(0, 0, 0))
		builder.define(FALL_DIRECTION, Direction.NORTH)
		builder.define(TREE_TYPE_LOCATION, "")
	}

	override fun onSyncedDataUpdated(dataAccessor: EntityDataAccessor<*>) {
		super.onSyncedDataUpdated(dataAccessor)
		if (TREE_TYPE_LOCATION == dataAccessor) {
			this.treeType = treeRegistry.getValue(Identifier.tryParse(this.getEntityData().get(TREE_TYPE_LOCATION)))!!
		}
	}

	override fun readAdditionalSaveData(valueInput: ValueInput) {}
	override fun addAdditionalSaveData(valueOutput: ValueOutput) {}

	override fun tick() {
		super.tick()

		if (!this.isNoGravity) {
			this.deltaMovement = this.deltaMovement.add(0.0, -0.04, 0.0)
		}
		this.move(MoverType.SELF, this.deltaMovement)
		if (this.onGround()) {
			this.deltaMovement = this.deltaMovement.multiply(1.0, -0.5, 1.0)
		}

		if (tickCount >= this.maxLifeTimeTick) {
			remove(RemovalReason.DISCARDED)
		}
	}

	override fun hurtServer(serverLevel: ServerLevel, damageSource: DamageSource, f: Float): Boolean {
		return false
	}

	val maxLifeTimeTick: Int = 80

	fun getLifetime(partialTick: Float): Float {
		return (this.tickCount + partialTick) / 20
	}

	val blocks: MutableMap<BlockPos, BlockState>
		get() = this.getEntityData().get(BLOCKS)

	val originPos: BlockPos
		get() = this.getEntityData().get(ORIGIN_POS)

	val fallDirection: Direction
		get() = this.getEntityData().get(FALL_DIRECTION)

	companion object {
		val BLOCKS: EntityDataAccessor<MutableMap<BlockPos, BlockState>> =
			SynchedEntityData.defineId(TreeEntity::class.java, BlockMapEntityData)
		val ORIGIN_POS: EntityDataAccessor<BlockPos> =
			SynchedEntityData.defineId(TreeEntity::class.java, EntityDataSerializers.BLOCK_POS)
		val FALL_DIRECTION: EntityDataAccessor<Direction> =
			SynchedEntityData.defineId(TreeEntity::class.java, EntityDataSerializers.DIRECTION)
		val TREE_TYPE_LOCATION: EntityDataAccessor<String> =
			SynchedEntityData.defineId(TreeEntity::class.java, EntityDataSerializers.STRING)
	}
}
