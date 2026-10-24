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
		// Unit block shape for rendering individual block colliders
		private val UNIT_BLOCK_SHAPE = Shapes.box(0.0, 0.0, 0.0, 1.0, 1.0, 1.0)
		// Small point shape (12cm box) for rendering probe contact points
		private val PROBE_POINT_SHAPE = Shapes.box(-0.06, -0.06, -0.06, 0.06, 0.06, 0.06)

		// Bright vibrant debug outline colors (ARGB)
		private const val COLOR_COLLIDER_BOX = 0xFF00FF00.toInt() // Green outline for collider blocks
		private const val COLOR_PROBE_POINT = 0xFFFF2222.toInt()  // Red outline for contact probe points
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

		val dir = state.fallDirection
		// Perpendicular axis: rotAxis = (dir.z, 0, -dir.x)
		val rotAxisX = dir.z.toFloat()
		val rotAxisZ = (-dir.x).toFloat()

		// Pivot around the outermost base block edge in the direction of the fall
		val p = state.pivotOffset
		poseStack.translate(p.x, p.y, p.z)
		poseStack.mulPose(Axis.of(Vector3f(rotAxisX, 0.0f, rotAxisZ)).rotationDegrees(state.fallAngle))
		poseStack.translate(-p.x, -p.y, -p.z)

		// Render each block in the tree at its relative offset
		for (entry in state.blockRenderStates) {
			val rel = entry.relativePos
			poseStack.pushPose()
			// Center block relative to entity center (-0.5 on x/z for block model centering)
			poseStack.translate(rel.x.toDouble() - 0.5, rel.y.toDouble(), rel.z.toDouble() - 0.5)
			submitNodeCollector.submitMovingBlock(poseStack, entry.movingBlockRenderState, state.outlineColor)
			poseStack.popPose()
		}

		// When F3+B (Hitboxes) is enabled, render outlines of all active colliders and contact test probes
		if (state.showDebugColliders && state.colliderOffsets.isNotEmpty()) {
			val linesType = RenderTypes.lines()

			// Primary fall direction vector and lateral perpendicular vector for contact probes
			val leadX = dir.x * 0.48
			val leadZ = dir.z * 0.48
			val latX = -dir.z * 0.45
			val latZ = dir.x * 0.45

			val probeDeltas = arrayOf(
				Triple(leadX, -0.45, leadZ),
				Triple(leadX + latX, -0.45, leadZ + latZ),
				Triple(leadX - latX, -0.45, leadZ - latZ),
				Triple(leadX, 0.45, leadZ),
				Triple(0.0, -0.48, 0.0),
				Triple(latX, -0.45, latZ),
				Triple(-latX, -0.45, -latZ)
			)

			for (offset in state.colliderOffsets) {
				// Green outline around each solid block that participates in collisions
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

				// Red points at each exact surface contact probe checked against terrain
				for (delta in probeDeltas) {
					poseStack.pushPose()
					poseStack.translate(offset.x + delta.first, offset.y + delta.second, offset.z + delta.third)
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
