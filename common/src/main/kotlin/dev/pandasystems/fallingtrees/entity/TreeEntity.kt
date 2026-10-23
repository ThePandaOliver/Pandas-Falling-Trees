package dev.pandasystems.fallingtrees.entity

import dev.pandasystems.fallingtrees.FallingTreesRegistries
import dev.pandasystems.fallingtrees.config.fallingTreesCommonConfig
import net.minecraft.core.BlockPos
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.tags.BlockTags
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector3fc
import java.util.UUID
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

class TreeEntity(type: EntityType<*>, level: Level) : Entity(type, level) {
	companion object {
		val DATA_FALL_DIRECTION: EntityDataAccessor<Vector3fc> =
			SynchedEntityData.defineId(TreeEntity::class.java, EntityDataSerializers.VECTOR3)

		val DATA_BLOCKS_STRING: EntityDataAccessor<String> =
			SynchedEntityData.defineId(TreeEntity::class.java, EntityDataSerializers.STRING)

		private const val INITIAL_ANGULAR_VELOCITY = 0.12f
		private const val BASE_GRAVITY_FACTOR = 0.38f
		private const val MAX_FALL_SPEED = 4.2f
		private const val BOUNCE_RESTITUTION = 0.65f
		private const val MIN_BOUNCE_VELOCITY = 0.45f
		private const val RESTING_DELAY_TICKS = 25
		private const val MAX_LIFETIME_TICKS = 240
	}

	val treeBlocks: MutableMap<BlockPos, BlockState> = LinkedHashMap()

	private var colliderOffsets: List<Vec3> = emptyList()
	private var trunkBlockCount: Int = 1
	private val damagedEntities: MutableSet<UUID> = HashSet()

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

	var prevFallAngle: Float = 0f
		private set

	var fallAngle: Float = 0f
		private set

	var angularVelocity: Float = INITIAL_ANGULAR_VELOCITY
		private set

	private var massScale: Float = 1.0f
	private var maxSpeedForSize: Float = MAX_FALL_SPEED
	private var bounceCount: Int = 0
	private var restingTicks: Int = 0
	private var totalTicks: Int = 0
	private var hasPlayedImpactSound: Boolean = false

	init {
		blocksBuilding = true
	}

	fun getInterpolatedFallAngle(partialTick: Float): Float =
		prevFallAngle + (fallAngle - prevFallAngle) * partialTick

	fun transformOffsetAroundEdgePivot(relOffset: Vec3, quat: Quaternionf, out: Vector3f): Vector3f {
		val dir = fallDirection
		val edgeX = (dir.x * 0.5).toFloat()
		val edgeZ = (dir.z * 0.5).toFloat()

		// Shift relative to the bottom edge pivot, rotate, then shift back
		out.set(
			relOffset.x.toFloat() - edgeX,
			relOffset.y.toFloat(),
			relOffset.z.toFloat() - edgeZ
		)
		quat.transform(out)
		out.add(edgeX, 0f, edgeZ)
		return out
	}

	fun initializeTree(
		origin: BlockPos,
		blocks: Map<BlockPos, BlockState>,
		drops: List<Pair<BlockPos, ItemStack>>,
		fallDir: Vec3
	) {
		setPos(origin.x + 0.5, origin.y.toDouble(), origin.z + 0.5)
		treeBlocks.clear()

		for ((worldPos, state) in blocks) {
			val rel = worldPos.subtract(origin)
			treeBlocks[rel] = state
		}

		rebuildGeometryBounds()

		pendingDrops.clear()
		pendingDrops.addAll(drops)
		fallDirection = fallDir

		entityData.set(DATA_BLOCKS_STRING, encodeBlocksToString(treeBlocks))
	}

	override fun defineSynchedData(entityData: SynchedEntityData.Builder) {
		entityData.define(DATA_FALL_DIRECTION, Vector3f(1f, 0f, 0f))
		entityData.define(DATA_BLOCKS_STRING, "")
	}

	override fun onSyncedDataUpdated(accessor: EntityDataAccessor<*>) {
		super.onSyncedDataUpdated(accessor)
		if (accessor == DATA_BLOCKS_STRING) {
			val encoded = entityData.get(DATA_BLOCKS_STRING)
			treeBlocks.clear()
			treeBlocks.putAll(decodeBlocksFromString(encoded))
			rebuildGeometryBounds()
		}
	}

	private fun rebuildGeometryBounds() {
		var minY = 0
		var maxY = 1
		val trunkSample = mutableListOf<Vec3>()
		val allSample = mutableListOf<Vec3>()

		for ((rel, state) in treeBlocks) {
			if (rel.y < minY) minY = rel.y
			if (rel.y > maxY) maxY = rel.y

			// Center of this block relative to (origin.x + 0.5, origin.y, origin.z + 0.5)
			val offset = Vec3(rel.x.toDouble(), rel.y + 0.5, rel.z.toDouble())
			allSample.add(offset)
			if (!state.`is`(BlockTags.LEAVES) && !state.`is`(BlockTags.WART_BLOCKS)) {
				trunkSample.add(offset)
			}
		}

		trunkBlockCount = max(1, trunkSample.size)
		val height = max(2, maxY - minY + 1).toFloat()
		val trunkCount = trunkBlockCount.toFloat()
		val totalBlocks = max(1, treeBlocks.size).toFloat()

		// Compute rotational inertia scale from height, trunk volume, and canopy weight
		// Normal trees (~22 effectiveMass) keep ~0.85 massScale, while giant trees gently taper down to 0.55
		val effectiveMass = height * 1.5f + sqrt(trunkCount) * 3.0f + sqrt(totalBlocks) * 0.8f
		massScale = 0.85f * (22.0f / effectiveMass.coerceAtLeast(10.0f)).pow(0.32f).coerceIn(0.65f, 1.25f)
		maxSpeedForSize = (MAX_FALL_SPEED * massScale.pow(0.25f)).coerceIn(3.2f, 4.8f)

		// Prefer solid trunk blocks for collision checks so outer leaves can compress slightly before solid impact
		val sourceOffsets = trunkSample.ifEmpty { allSample }
		colliderOffsets = if (sourceOffsets.size <= 64) {
			sourceOffsets
		} else {
			// Prioritize blocks higher up the tree
			sourceOffsets.sortedByDescending { it.lengthSqr() }.take(64)
		}

		val expand = (maxY - minY + 3).toDouble()
		boundingBox = AABB(
			x - expand, y - expand, z - expand,
			x + expand, y + expand, z + expand
		)
	}

	override fun tick() {
		super.tick()
		totalTicks++
		prevFallAngle = fallAngle

		val currentLevel = level()

		// If the tree has come to rest after bouncing, count down before despawning & dropping items
		if (restingTicks > 0) {
			restingTicks++
			if (!currentLevel.isClientSide && (restingTicks >= RESTING_DELAY_TICKS || totalTicks >= MAX_LIFETIME_TICKS)) {
				onTreeFinish(currentLevel as ServerLevel)
				discard()
			}
			return
		}

		// Inverted pendulum rotational gravity:
		// Starts very slow near 0 deg, accelerates smoothly, and is damped by canopy air resistance near the end
		val rad = Math.toRadians(fallAngle.toDouble())
		val torqueCurve = max(0.06, sin(rad).pow(0.75) + 0.04 * cos(rad)).toFloat()
		val angularAccel = BASE_GRAVITY_FACTOR * torqueCurve * massScale

		angularVelocity += angularAccel
		// Canopy air drag prevents the end of the fall from whipping too fast
		angularVelocity = (angularVelocity * 0.965f).coerceAtMost(maxSpeedForSize)

		val targetAngle = fallAngle + angularVelocity

		// Check if sweeping to targetAngle collides with the environment
		if (fallAngle > 5.0f && angularVelocity > 0f && checkCollisionAtAngle(currentLevel, targetAngle)) {
			// Binary search for exact contact angle between fallAngle and targetAngle
			var low = fallAngle
			var high = targetAngle
			for (i in 0 until 4) {
				val mid = (low + high) * 0.5f
				if (checkCollisionAtAngle(currentLevel, mid)) {
					high = mid
				} else {
					low = mid
				}
			}
			fallAngle = low

			// Trigger primary impact sound on the first major collision
			if (!hasPlayedImpactSound && !currentLevel.isClientSide) {
				hasPlayedImpactSound = true
				playImpactSound(currentLevel as ServerLevel, 1.0f)
			}

			val bounceFactor = when (bounceCount) {
				0 -> BOUNCE_RESTITUTION
				1 -> BOUNCE_RESTITUTION * 0.55f
				else -> BOUNCE_RESTITUTION * 0.3f
			}
			val bounceSpeed = (angularVelocity * bounceFactor).coerceAtLeast(
				if (bounceCount == 0) 1.5f * massScale.coerceAtLeast(0.6f) else 0f
			)

			if (bounceSpeed >= MIN_BOUNCE_VELOCITY && bounceCount < 3) {
				angularVelocity = -bounceSpeed
				bounceCount++

				// Subtle secondary thud on bounce
				if (bounceCount > 1 && !currentLevel.isClientSide) {
					playImpactSound(currentLevel as ServerLevel, (0.5f / bounceCount).coerceAtLeast(0.18f))
				}
			} else {
				angularVelocity = 0f
				restingTicks = 1
			}
		} else {
			fallAngle = targetAngle.coerceAtMost(135.0f)
			if (fallAngle >= 135.0f) {
				angularVelocity = 0f
				restingTicks = 1
			}
		}

		// Deal falling tree damage to entities hit while the tree is actively falling downward
		if (!currentLevel.isClientSide && fallAngle > 12.0f && angularVelocity > 0.35f) {
			damageEntitiesInPath(currentLevel as ServerLevel)
		}

		if (!currentLevel.isClientSide && totalTicks >= MAX_LIFETIME_TICKS) {
			onTreeFinish(currentLevel as ServerLevel)
			discard()
		}
	}

	private fun damageEntitiesInPath(serverLevel: ServerLevel) {
		val config = fallingTreesCommonConfig.value
		if (!config.enableFallingTreeDamage || colliderOffsets.isEmpty()) return

		val candidates = serverLevel.getEntitiesOfClass(LivingEntity::class.java, boundingBox) { entity ->
			entity.isAlive && !entity.isSpectator && entity.uuid !in damagedEntities
		}
		if (candidates.isEmpty()) return

		val dir = fallDirection
		val rotAxisX = dir.z.toFloat()
		val rotAxisZ = (-dir.x).toFloat()
		val quat = Quaternionf().rotationAxis(Math.toRadians(fallAngle.toDouble()).toFloat(), rotAxisX, 0f, rotAxisZ)

		val pivotX = x
		val pivotY = y
		val pivotZ = z
		val leadX = dir.x * 0.45
		val leadZ = dir.z * 0.45
		val tempVec = Vector3f()

		val speedRatio = (angularVelocity / maxSpeedForSize.coerceAtLeast(1.0f)).coerceIn(0.25f, 1.25f)
		val rawDamage = (config.baseFallingTreeDamage + trunkBlockCount * config.damagePerTrunkBlock) * speedRatio
		val finalDamage = rawDamage.coerceIn(1.0f, config.maxFallingTreeDamage)
		val damageSource = damageSources().fallingBlock(this)

		for (entity in candidates) {
			val entityBox = entity.boundingBox
			var hit = false
			for (offset in colliderOffsets) {
				// Ignore the bottom pivot log block so the player chopping at the base isn't immediately hit
				if (offset.y < 1.0 && offset.x == 0.0 && offset.z == 0.0) continue

				// Check both the block center and the leading face of the block in the fall direction
				val leadOffset = Vec3(offset.x + leadX, offset.y, offset.z + leadZ)
				transformOffsetAroundEdgePivot(leadOffset, quat, tempVec)
				val wx = pivotX + tempVec.x
				val wy = pivotY + tempVec.y
				val wz = pivotZ + tempVec.z

				if (entityBox.intersects(
						wx - 0.45, wy - 0.45, wz - 0.45,
						wx + 0.45, wy + 0.45, wz + 0.45
					)
				) {
					hit = true
					break
				}
			}

			if (hit) {
				damagedEntities.add(entity.uuid)
				entity.hurtServer(serverLevel, damageSource, finalDamage)
				// Apply a small knockback in the direction the tree is falling
				entity.push(dir.x * 0.35, 0.15, dir.z * 0.35)
			}
		}
	}

	private fun checkCollisionAtAngle(level: Level, angleDeg: Float): Boolean {
		if (colliderOffsets.isEmpty()) {
			return angleDeg >= 90.0f
		}

		val dir = fallDirection
		val rotAxisX = dir.z.toFloat()
		val rotAxisZ = (-dir.x).toFloat()
		val quat = Quaternionf().rotationAxis(Math.toRadians(angleDeg.toDouble()).toFloat(), rotAxisX, 0f, rotAxisZ)

		val pivotX = x
		val pivotY = y
		val pivotZ = z
		val originBlock = blockPosition()

		// Offset from the block's center to its leading side face (and top edge) in the direction of the fall
		val sideX = dir.x * 0.48
		val sideZ = dir.z * 0.48

		val tempVec = Vector3f()
		val checkPos = BlockPos.MutableBlockPos()

		for (offset in colliderOffsets) {
			// Skip the bottom pivot block itself so it doesn't immediately collide with the ground it sits on
			if (offset.y < 1.0 && offset.x == 0.0 && offset.z == 0.0) continue

			// Check the leading side face of the falling block (which becomes the bottom face at 90 degrees)
			// plus a point near its top leading edge so tall blocks don't clip at the tip
			for (yShift in floatArrayOf(0.0f, 0.42f)) {
				val sidePoint = Vec3(offset.x + sideX, offset.y + yShift, offset.z + sideZ)
				transformOffsetAroundEdgePivot(sidePoint, quat, tempVec)

				val wx = pivotX + tempVec.x
				val wy = pivotY + tempVec.y
				val wz = pivotZ + tempVec.z

				checkPos.set(wx, wy, wz)
				if (checkPos == originBlock) continue

				val state = level.getBlockState(checkPos)
				if (state.isAir) continue

				val shape = state.getCollisionShape(level, checkPos)
				if (!shape.isEmpty) {
					val worldBounds = shape.bounds().move(checkPos)
					if (worldBounds.intersects(
							wx - 0.04, wy - 0.04, wz - 0.04,
							wx + 0.04, wy + 0.04, wz + 0.04
						)
					) {
						return true
					}
				}
			}
		}

		return false
	}

	private fun playImpactSound(serverLevel: ServerLevel, volume: Float) {
		serverLevel.playSound(
			null,
			blockPosition(),
			FallingTreesRegistries.treeImpactSound.get(),
			SoundSource.BLOCKS,
			volume,
			0.85f + serverLevel.random.nextFloat() * 0.25f
		)
	}

	private fun onTreeFinish(serverLevel: ServerLevel) {
		if (!hasPlayedImpactSound) {
			hasPlayedImpactSound = true
			playImpactSound(serverLevel, 1.0f)
		}

		val dir = fallDirection
		val rotAxisX = dir.z.toFloat()
		val rotAxisZ = (-dir.x).toFloat()
		val quat = Quaternionf().rotationAxis(Math.toRadians(fallAngle.toDouble()).toFloat(), rotAxisX, 0f, rotAxisZ)

		val originPos = blockPosition()
		val tempVec = Vector3f()

		for ((dropPos, stack) in pendingDrops) {
			if (!stack.isEmpty) {
				// Spawn drops along the rotated fallen position of the tree
				val relOffset = Vec3(
					(dropPos.x - originPos.x).toDouble(),
					(dropPos.y - originPos.y).toDouble() + 0.5,
					(dropPos.z - originPos.z).toDouble()
				)
				transformOffsetAroundEdgePivot(relOffset, quat, tempVec)

				val targetDropPos = BlockPos.containing(x + tempVec.x, y + tempVec.y, z + tempVec.z)
				Block.popResource(serverLevel, targetDropPos, stack)
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
