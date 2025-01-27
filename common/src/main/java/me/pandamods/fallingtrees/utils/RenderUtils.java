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

package me.pandamods.fallingtrees.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import me.pandamods.fallingtrees.mixin.accessor.BlockRenderDispatcherAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class RenderUtils {
	public static void renderSingleBlock(PoseStack poseStack, BlockState blockState, BlockPos blockPos,
										 BlockAndTintGetter level, MultiBufferSource bufferSource, int packedLight) {
		int packedOverlay = OverlayTexture.NO_OVERLAY;
		BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
		BlockRenderDispatcherAccessor accessor = (BlockRenderDispatcherAccessor) blockRenderDispatcher;

		BakedModel bakedModel = blockRenderDispatcher.getBlockModel(blockState);
		int color = blockRenderDispatcher.blockColors.getColor(blockState, level, blockPos, 0);
		float red = (float)(color >> 16 & 255) / 255.0F;
		float green = (float)(color >> 8 & 255) / 255.0F;
		float blue = (float)(color & 255) / 255.0F;
		blockRenderDispatcher.getModelRenderer()
				.renderModel(poseStack.last(), bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(blockState)), blockState, bakedModel, red, green, blue, packedLight, packedOverlay);
		accessor.getSpecialBlockModelRenderer().get()
				.renderByBlock(blockState.getBlock(), ItemDisplayContext.NONE, poseStack, bufferSource, packedLight, packedOverlay);
	}
}
