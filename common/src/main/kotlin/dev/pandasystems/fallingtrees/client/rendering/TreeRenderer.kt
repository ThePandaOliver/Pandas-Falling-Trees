package dev.pandasystems.fallingtrees.client.rendering

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.pandasystems.fallingtrees.client.rendering.state.TreeRenderState
import dev.pandasystems.fallingtrees.entity.TreeEntity
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.debug.DebugScreenEntries
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.block.MovingBlockRenderState
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.phys.shapes.Shapes
import org.joml.Vector3f

class TreeRenderer(context: EntityRendererProvider.Context) : EntityRenderer<TreeEntity, TreeRenderState>(context) {
	companion object {
		private val UNIT_BLOCK_SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
		private val PROBE_POINT_SHAPE = Shapes.box(-0.06, -0.06, -0.06, 0.06, 0.06, 0.06)

		private const val COLOR_COLLIDER_BOX = 0xFF00FF00.toInt()
		private const val COLOR_PROBE_POINT = 0xFFFF2222.toInt()
	}

	init {
		shadowRadius = 0.5f
	}

	override fun createRenderState(): TreeRenderState = TreeRenderState()

	override fun shouldRender(entity: TreeEntity, culler: Frustum, camX: Double, camY: Double, camZ: Double): Boolean {
		return true
	}

	override fun extractRenderState(entity: TreeEntity, state: TreeRenderState, partialTick: Float) {
		super.extractRenderState(entity, state, partialTick)
		state.fallDirection = entity.fallDirection
		state.fallAngle = entity.getInterpolatedFallAngle(partialTick)
		state.pivotOffset = entity.pivotOffset
		state.colliderOffsets = entity.colliderOffsets

		val mc = Minecraft.getInstance()
		state.showDebugColliders = mc.debugEntries.isCurrentlyEnabled(DebugScreenEntries.ENTITY_HITBOXES)

		val clientLevel = entity.level() as? ClientLevel
		val originPos = entity.blockPosition()

		val renderList = mutableListOf<TreeRenderState.SingleBlockState>()
		for ((relPos, blockState) in entity.treeBlocks) {
			if (blockState.renderShape != RenderShape.MODEL) continue

			val moving = MovingBlockRenderState()
			val worldPos = originPos.offset(relPos)
			moving.randomSeedPos = worldPos
			moving.blockPos = worldPos
			moving.blockState = blockState

			if (clientLevel != null) {
				moving.biome = clientLevel.getBiome(worldPos)
				moving.cardinalLighting = clientLevel.cardinalLighting()
				moving.lightEngine = clientLevel.lightEngine
			}

			renderList.add(TreeRenderState.SingleBlockState(relPos, moving))
		}
		state.blockRenderStates = renderList
	}

	override fun submit(
		state: TreeRenderState,
		poseStack: PoseStack,
		submitNodeCollector: SubmitNodeCollector,
		camera: CameraRenderState
	) {
		if (state.blockRenderStates.isEmpty()) {
			super.submit(state, poseStack, submitNodeCollector, camera)
			return
		}

		poseStack.pushPose()

		val direction = state.fallDirection
		val rotAxisX = direction.z.toFloat()
		val rotAxisZ = (-direction.x).toFloat()

		val pivot = state.pivotOffset
		poseStack.translate(pivot.x, pivot.y, pivot.z)
		poseStack.mulPose(Axis.of(Vector3f(rotAxisX, 0.0f, rotAxisZ)).rotationDegrees(state.fallAngle))
		poseStack.translate(-pivot.x, -pivot.y, -pivot.z)

		for (entry in state.blockRenderStates) {
			val relativePos = entry.relativePos
			poseStack.pushPose()
			poseStack.translate(relativePos.x.toDouble() - 0.5, relativePos.y.toDouble(), relativePos.z.toDouble() - 0.5)
			submitNodeCollector.submitMovingBlock(poseStack, entry.movingBlockRenderState, state.outlineColor)
			poseStack.popPose()
		}

		if (state.showDebugColliders && state.colliderOffsets.isNotEmpty()) {
			val linesType = RenderTypes.lines()

			val leadX = direction.x * 0.5
			val leadZ = direction.z * 0.5
			val latX = -direction.z * 0.5
			val latZ = direction.x * 0.5

			val probeDeltas = arrayOf(
				Triple(leadX, -0.5, leadZ),
				Triple(leadX + latX, -0.5, leadZ + latZ),
				Triple(leadX - latX, -0.5, leadZ - latZ),
				Triple(leadX, 0.5, leadZ),
				Triple(0.0, -0.5, 0.0),
				Triple(latX, -0.5, latZ),
				Triple(-latX, -0.5, -latZ)
			)

			for (offset in state.colliderOffsets) {
				poseStack.pushPose()
				poseStack.translate(offset.x - 0.5, offset.y - 0.5, offset.z - 0.5)
				submitNodeCollector.submitShapeOutline(
					poseStack,
					UNIT_BLOCK_SHAPE,
					linesType,
					COLOR_COLLIDER_BOX,
					2.5f,
					false
				)
				poseStack.popPose()

				for ((first, second, third) in probeDeltas) {
					poseStack.pushPose()
					poseStack.translate(offset.x + first, offset.y + second, offset.z + third)
					submitNodeCollector.submitShapeOutline(
						poseStack,
						PROBE_POINT_SHAPE,
						linesType,
						COLOR_PROBE_POINT,
						2.0f,
						false
					)
					poseStack.popPose()
				}
			}
		}

		poseStack.popPose()

		super.submit(state, poseStack, submitNodeCollector, camera)
	}
}
