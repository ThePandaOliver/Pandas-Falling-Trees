/*
 * Copyright (C) 2025 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package dev.pandasystems.fallingtrees.client.render

import com.mojang.blaze3d.vertex.PoseStack
import dev.pandasystems.fallingtrees.api.TreeType
import dev.pandasystems.fallingtrees.config.fallingTreesClientConfig
import dev.pandasystems.fallingtrees.entity.TreeEntity
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.state.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.state.BlockState
import org.joml.Math
import org.joml.Quaternionf
import org.joml.Vector3f

class TreeRenderer(context: EntityRendererProvider.Context) : EntityRenderer<TreeEntity, TreeRenderState>(context) {
	val config get() = fallingTreesClientConfig.get()

	override fun submit(renderState: TreeRenderState, poseStack: PoseStack, nodeCollector: SubmitNodeCollector, cameraRenderState: CameraRenderState) {
		super.submit(renderState, poseStack, nodeCollector, cameraRenderState)

		val tree = renderState.treeType ?: return

		poseStack.pushPose()

		val blocks: MutableMap<BlockPos, BlockState> = renderState.blocks!!
		val fallAnimLength: Float = this.config.animation.fallAnimLength

		val bounceHeight: Float = this.config.animation.bounceAngleHeight
		val bounceAnimLength: Float = this.config.animation.bounceAnimLength

		val time = (renderState.lifeTime * (Math.PI / 2) / fallAnimLength).toFloat()

		val fallAnim = bumpCos(time) * 90
		val bounceAnim = bumpSin(((time - Math.PI / 2) / (bounceAnimLength / (fallAnimLength * 2))).toFloat()) * bounceHeight

		val animation = (fallAnim + bounceAnim) - 90

		val direction = renderState.direction!!.opposite
		val distance = getDistance(tree, blocks, direction.opposite)

		val pivot = Vector3f(0f, 0f, .5f + distance)
		pivot.rotateY(Math.toRadians(-direction.toYRot()))
		poseStack.translate(-pivot.x, 0f, -pivot.z)

		val vector = Vector3f(Math.toRadians(animation), 0f, 0f)
		vector.rotateY(Math.toRadians(-direction.toYRot()))
		val quaternion = Quaternionf().identity().rotateX(vector.x).rotateZ(vector.z)
		poseStack.mulPose(quaternion)

		val level = renderState.level!!

		poseStack.translate(pivot.x, 0f, pivot.z)
		poseStack.translate(-.5, 0.0, -.5)
		blocks.forEach { (blockPos, blockState) ->
			poseStack.pushPose()
			poseStack.translate(blockPos.x.toFloat(), blockPos.y.toFloat(), blockPos.z.toFloat())
			nodeCollector.submitBlock(poseStack, blockState, renderState.lightCoords, OverlayTexture.NO_OVERLAY, renderState.outlineColor)
			poseStack.popPose()
		}
		poseStack.popPose()
	}

	override fun createRenderState(): TreeRenderState {
		return TreeRenderState()
	}

	override fun extractRenderState(entity: TreeEntity, renderState: TreeRenderState, f: Float) {
		renderState.treeType = entity.treeType
		renderState.blocks = entity.blocks
		renderState.lifeTime = entity.getLifetime(f).toDouble()
		renderState.direction = entity.direction
		renderState.originPos = entity.originPos
		renderState.level = entity.level()
		super.extractRenderState(entity, renderState, f)
	}

	private fun getDistance(tree: TreeType, blocks: MutableMap<BlockPos, BlockState>, direction: Direction): Float {
		var distance = 0f
		var currentPos = BlockPos(0, 0, 0)
		var next = currentPos.relative(direction)

		while (blocks.containsKey(next)) {
			if (!tree.isTreeStem(blocks[next]!!)) break

			currentPos = next
			next = currentPos.relative(direction)

			distance++
		}
		val blockState: BlockState = blocks[currentPos]!!
		if (blockState.hasOffsetFunction()) return distance - .5f

		var shape = blockState.getCollisionShape(Minecraft.getInstance().level!!, currentPos)
		if (shape.isEmpty) shape = blockState.getShape(Minecraft.getInstance().level!!, currentPos)

		if (!shape.isEmpty) {
			val bounds = shape.bounds()
			when (direction) {
				Direction.WEST -> distance -= (bounds.minX).toFloat()
				Direction.EAST -> distance -= (1f - bounds.maxX).toFloat()
				Direction.SOUTH -> distance -= (bounds.minZ).toFloat()
				Direction.NORTH -> distance -= (1f - bounds.maxZ).toFloat()
				else -> 0
			}
		} else {
			distance -= 1f
		}
		return distance
	}

	private fun bumpCos(time: Float): Float {
		return Math.max(0.0, Math.cos(Math.clamp(-Math.PI, Math.PI, time.toDouble()))).toFloat()
	}

	private fun bumpSin(time: Float): Float {
		return Math.max(0.0, Math.sin(Math.clamp(-Math.PI, Math.PI, time.toDouble()))).toFloat()
	}

	override fun affectedByCulling(entity: TreeEntity?): Boolean {
		return false
	}
}
