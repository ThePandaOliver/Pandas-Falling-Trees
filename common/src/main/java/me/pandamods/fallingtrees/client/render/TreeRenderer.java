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

package me.pandamods.fallingtrees.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.pandamods.fallingtrees.api.TreeType;
import me.pandamods.fallingtrees.config.ClientConfig;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import me.pandamods.fallingtrees.entity.TreeEntity;
import me.pandamods.fallingtrees.utils.RenderUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class TreeRenderer extends EntityRenderer<TreeEntity, TreeRenderState> {
	public TreeRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	public ClientConfig getConfig() {
		return FallingTreesConfig.getClientConfig(Minecraft.getInstance().player);
	}

	@Override
	public void render(TreeRenderState renderState, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
		TreeType tree = renderState.treeType;
		if (tree == null) return;

		poseStack.pushPose();

		Map<BlockPos, BlockState> blocks = renderState.blocks;
		float fallAnimLength = getConfig().animation.fallAnimLength;

		float bounceHeight = getConfig().animation.bounceAngleHeight;
		float bounceAnimLength = getConfig().animation.bounceAnimLength;

		float time = (float) (renderState.lifeTime * (Math.PI / 2) / fallAnimLength);

		float fallAnim = bumpCos(time) * 90;
		float bounceAnim = bumpSin((float) ((time - Math.PI / 2) / (bounceAnimLength / (fallAnimLength * 2)))) * bounceHeight;

		float animation = (fallAnim + bounceAnim) - 90;

		Direction direction = renderState.direction.getOpposite();
		int distance = getDistance(tree, blocks, 0, direction.getOpposite());

		Vector3f pivot =  new Vector3f(0, 0, (.5f + distance) * 1);
		pivot.rotateY(Math.toRadians(-direction.toYRot()));
		poseStack.translate(-pivot.x, 0, -pivot.z);

		Vector3f vector = new Vector3f(Math.toRadians(animation), 0, 0);
		vector.rotateY(Math.toRadians(-direction.toYRot()));
		Quaternionf quaternion = new Quaternionf().identity().rotateX(vector.x).rotateZ(vector.z);
		poseStack.mulPose(quaternion);

		Level level = renderState.level;

		poseStack.translate(pivot.x, 0, pivot.z);

		poseStack.translate(-.5, 0, -.5);
		blocks.forEach((blockPos, blockState) -> {
			poseStack.pushPose();
			poseStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());

			blockPos = blockPos.offset(renderState.originPos);
			RenderUtils.renderSingleBlock(poseStack, blockState, blockPos, level, buffer, packedLight);

			poseStack.popPose();
		});
		poseStack.popPose();
	}

	@Override
	@NotNull
	public TreeRenderState createRenderState() {
		return new TreeRenderState();
	}

	@Override
	public void extractRenderState(TreeEntity entity, TreeRenderState renderState, float f) {
		renderState.treeType = entity.getTreeType();
		renderState.blocks = entity.getBlocks();
		renderState.lifeTime = entity.getLifetime(f);
		renderState.direction = entity.getDirection();
		renderState.originPos = entity.getOriginPos();
		renderState.level = entity.level();
		super.extractRenderState(entity, renderState, f);
	}

	private int getDistance(TreeType tree, Map<BlockPos, BlockState> blocks, int distance, Direction direction) {
		BlockPos nextBlockPos = new BlockPos(direction.getUnitVec3i().multiply(distance + 1));
		if (blocks.containsKey(nextBlockPos) && tree.isTreeStem(blocks.get(nextBlockPos)))
			return getDistance(tree, blocks, distance + 1, direction);
		return distance;
	}

	private float bumpCos(float time) {
		return (float) Math.max(0, Math.cos(Math.clamp(-Math.PI, Math.PI, time)));
	}

	private float bumpSin(float time) {
		return (float) Math.max(0, Math.sin(Math.clamp(-Math.PI, Math.PI, time)));
	}

	@Override
	protected boolean affectedByCulling(TreeEntity entity) {
		return false;
	}
}
