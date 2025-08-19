/*
 * Copyright (C) 2024 Oliver Froberg (The Panda Oliver)
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
import dev.pandasystems.fallingtrees.mixin.accessor.BlockRenderDispatcherAccessor
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.block.ModelBlockRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.level.BlockAndTintGetter
import net.minecraft.world.level.block.state.BlockState

object RenderUtils {
	@JvmStatic
	fun renderSingleBlock(
		poseStack: PoseStack, blockState: BlockState, blockPos: BlockPos,
		level: BlockAndTintGetter, bufferSource: MultiBufferSource, packedLight: Int
	) {
		val packedOverlay = OverlayTexture.NO_OVERLAY
		val blockRenderDispatcher = Minecraft.getInstance().blockRenderer
		val accessor = blockRenderDispatcher as BlockRenderDispatcherAccessor

		val bakedModel = blockRenderDispatcher.getBlockModel(blockState)
		val color = blockRenderDispatcher.blockColors.getColor(blockState, level, blockPos, 0)
		val red = (color shr 16 and 255).toFloat() / 255.0f
		val green = (color shr 8 and 255).toFloat() / 255.0f
		val blue = (color and 255).toFloat() / 255.0f
		ModelBlockRenderer.renderModel(
			poseStack.last(),
			bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(blockState)),
			bakedModel,
			red,
			green,
			blue,
			packedLight,
			packedOverlay
		)
		accessor.getSpecialBlockModelRenderer().get()
			.renderByBlock(blockState.block, ItemDisplayContext.NONE, poseStack, bufferSource, packedLight, packedOverlay)
	}
}
