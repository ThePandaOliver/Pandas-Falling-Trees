package dev.pandasystems.fallingtrees.client.rendering

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.pandasystems.fallingtrees.client.rendering.state.TreeRenderState
import dev.pandasystems.fallingtrees.entity.TreeEntity
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.block.MovingBlockRenderState
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.world.level.block.RenderShape
import org.joml.Vector3f

class TreeRenderer(context: EntityRendererProvider.Context) : EntityRenderer<TreeEntity, TreeRenderState>(context) {
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

		// Pivot around the bottom edge of the origin block in the direction of the fall
		val edgeX = dir.x * 0.5
		val edgeZ = dir.z * 0.5
		poseStack.translate(edgeX, 0.0, edgeZ)
		poseStack.mulPose(Axis.of(Vector3f(rotAxisX, 0.0f, rotAxisZ)).rotationDegrees(state.fallAngle))
		poseStack.translate(-edgeX, 0.0, -edgeZ)

		// Render each block in the tree at its relative offset
		for (entry in state.blockRenderStates) {
			val rel = entry.relativePos
			poseStack.pushPose()
			// Center block relative to entity center (-0.5 on x/z for block model centering)
			poseStack.translate(rel.x.toDouble() - 0.5, rel.y.toDouble(), rel.z.toDouble() - 0.5)
			submitNodeCollector.submitMovingBlock(poseStack, entry.movingBlockRenderState, state.outlineColor)
			poseStack.popPose()
		}

		poseStack.popPose()

		super.submit(state, poseStack, submitNodeCollector, camera)
	}
}
