package dev.pandasystems.fallingtrees.entity

import dev.pandasystems.fallingtrees.FallingTreesRegistries
import net.minecraft.core.BlockPos
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import org.joml.Vector3fc
import kotlin.collections.iterator

class TreeEntity(type: EntityType<*>, level: Level) : Entity(type, level) {
	companion object {
		val DATA_FALL_DIRECTION: EntityDataAccessor<Vector3fc> =
			SynchedEntityData.defineId(TreeEntity::class.java, EntityDataSerializers.VECTOR3)

		val DATA_MAX_FALL_TICKS: EntityDataAccessor<Int> =
			SynchedEntityData.defineId(TreeEntity::class.java, EntityDataSerializers.INT)

		val DATA_BLOCKS_STRING: EntityDataAccessor<String> =
			SynchedEntityData.defineId(TreeEntity::class.java, EntityDataSerializers.STRING)
	}

	val treeBlocks: MutableMap<BlockPos, BlockState> = LinkedHashMap()

	val pendingDrops: MutableList<Pair<BlockPos, ItemStack>> = mutableListOf()

	var fallDirection: Vec3
		get() {
			val vec = entityData.get(DATA_FALL_DIRECTION)
			return Vec3(vec.x().toDouble(), vec.y().toDouble(), vec.z().toDouble())
		}
		set(value) {
			val norm = if (value.horizontalDistanceSqr() > 1.0e-4) {
				Vec3(value.x, 0.0, value.z).normalize()
			} else {
				Vec3(1.0, 0.0, 0.0)
			}
			entityData.set(DATA_FALL_DIRECTION, Vector3f(norm.x.toFloat(), norm.y.toFloat(), norm.z.toFloat()))
		}

	var maxFallTicks: Int
		get() = entityData.get(DATA_MAX_FALL_TICKS)
		set(value) {
			entityData.set(DATA_MAX_FALL_TICKS, value.coerceAtLeast(1))
		}

	var currentFallTicks: Int = 0
		private set

	val fallProgress: Float
		get() = (currentFallTicks.toFloat() / maxFallTicks.coerceAtLeast(1)).coerceIn(0f, 1f)

	init {
		blocksBuilding = true
	}

	fun initializeTree(
		origin: BlockPos,
		blocks: Map<BlockPos, BlockState>,
		drops: List<Pair<BlockPos, ItemStack>>,
		fallDir: Vec3
	) {
		setPos(origin.x + 0.5, origin.y.toDouble(), origin.z + 0.5)
		treeBlocks.clear()

		var minX = 0
		var maxX = 0
		var minY = 0
		var maxY = 0
		var minZ = 0
		var maxZ = 0

		for ((worldPos, state) in blocks) {
			val rel = worldPos.subtract(origin)
			treeBlocks[rel] = state
			if (rel.x < minX) minX = rel.x
			if (rel.x > maxX) maxX = rel.x
			if (rel.y < minY) minY = rel.y
			if (rel.y > maxY) maxY = rel.y
			if (rel.z < minZ) minZ = rel.z
			if (rel.z > maxZ) maxZ = rel.z
		}

		val expand = (maxY - minY + 2).toDouble()
		boundingBox = AABB(
			x - expand, y - 2.0, z - expand,
			x + expand, y + expand + 2.0, z + expand
		)

		pendingDrops.clear()
		pendingDrops.addAll(drops)
		fallDirection = fallDir

		entityData.set(DATA_BLOCKS_STRING, encodeBlocksToString(treeBlocks))
	}

	override fun defineSynchedData(entityData: SynchedEntityData.Builder) {
		entityData.define(DATA_FALL_DIRECTION, Vector3f(1f, 0f, 0f))
		entityData.define(DATA_MAX_FALL_TICKS, 35)
		entityData.define(DATA_BLOCKS_STRING, "")
	}

	override fun onSyncedDataUpdated(accessor: EntityDataAccessor<*>) {
		super.onSyncedDataUpdated(accessor)
		if (accessor == DATA_BLOCKS_STRING) {
			val encoded = entityData.get(DATA_BLOCKS_STRING)
			treeBlocks.clear()
			treeBlocks.putAll(decodeBlocksFromString(encoded))

			val maxY = treeBlocks.keys.maxOfOrNull { it.y } ?: 4
			val expand = (maxY + 2).toDouble()
			boundingBox = AABB(
				x - expand, y - 2.0, z - expand,
				x + expand, y + expand + 2.0, z + expand
			)
		}
	}

	override fun tick() {
		super.tick()
		currentFallTicks++

		val currentLevel = level()
		if (!currentLevel.isClientSide && currentFallTicks >= maxFallTicks) {
			onTreeImpact(currentLevel as ServerLevel)
			discard()
		}
	}

	private fun onTreeImpact(serverLevel: ServerLevel) {
		serverLevel.playSound(
			null,
			blockPosition(),
			FallingTreesRegistries.treeImpactSound.get(),
			SoundSource.BLOCKS,
			1.0f,
			0.9f + serverLevel.random.nextFloat() * 0.2f
		)

		for ((dropPos, stack) in pendingDrops) {
			if (!stack.isEmpty) {
				Block.popResource(serverLevel, dropPos, stack)
			}
		}
		pendingDrops.clear()
	}

	override fun hurtServer(
		level: ServerLevel,
		source: DamageSource,
		damage: Float
	): Boolean = false

	override fun readAdditionalSaveData(input: ValueInput) {}

	override fun addAdditionalSaveData(output: ValueOutput) {}

	private fun encodeBlocksToString(map: Map<BlockPos, BlockState>): String {
		val sb = StringBuilder()
		for ((pos, state) in map) {
			val stateId = Block.getId(state)
			sb.append(pos.x).append(',').append(pos.y).append(',').append(pos.z).append(':').append(stateId).append(';')
		}
		return sb.toString()
	}

	private fun decodeBlocksFromString(encoded: String): Map<BlockPos, BlockState> {
		val result = LinkedHashMap<BlockPos, BlockState>()
		if (encoded.isEmpty()) return result

		val entries = encoded.split(';')
		for (entry in entries) {
			if (entry.isEmpty()) continue
			val parts = entry.split(':')
			if (parts.size != 2) continue

			val coords = parts[0].split(',')
			if (coords.size != 3) continue

			val x = coords[0].toIntOrNull() ?: continue
			val y = coords[1].toIntOrNull() ?: continue
			val z = coords[2].toIntOrNull() ?: continue
			val stateId = parts[1].toIntOrNull() ?: continue

			val state = Block.stateById(stateId)
			if (!state.isAir) {
				result[BlockPos(x, y, z)] = state
			}
		}
		return result
	}
}
