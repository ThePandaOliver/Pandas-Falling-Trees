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

package dev.pandasystems.fallingtrees.utils

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.block.RenderShape
import net.minecraft.world.level.block.state.BlockState

object RenderUtils {
	@JvmStatic
	fun renderSingleBlock(
		poseStack: PoseStack, blockState: BlockState, blockPos: BlockPos,
		level: BlockAndTintGetter, bufferSource: MultiBufferSource, packedLight: Int
	) {
		val blockRenderDispatcher = Minecraft.getInstance().blockRenderer
		val blockEntityRenderDispatcher: BlockEntityWithoutLevelRenderer = blockRenderDispatcher.blockEntityRenderer
		val renderShape = blockState.renderShape
		when (renderShape) {
			RenderShape.MODEL -> {
				val bakedModel = blockRenderDispatcher.getBlockModel(blockState)
				val color = blockRenderDispatcher.blockColors.getColor(blockState, level, blockPos, 0)
				val red = (color shr 16 and 0xFF).toFloat() / 255.0f
				val green = (color shr 8 and 0xFF).toFloat() / 255.0f
				val blue = (color and 0xFF).toFloat() / 255.0f
				blockRenderDispatcher.modelRenderer.renderModel(
					poseStack.last(),
					bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(blockState, false)), blockState,
					bakedModel, red, green, blue, packedLight, OverlayTexture.NO_OVERLAY
				)
			}

			RenderShape.ENTITYBLOCK_ANIMATED -> blockEntityRenderDispatcher.renderByItem(
				ItemStack(blockState.block),
				ItemDisplayContext.NONE,
				poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY
			)

			else -> {}
		}
	}
}