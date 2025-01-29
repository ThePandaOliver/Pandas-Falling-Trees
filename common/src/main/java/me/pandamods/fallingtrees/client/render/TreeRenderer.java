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
import com.mojang.math.Quaternion;
import me.pandamods.fallingtrees.config.ClientConfig;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import me.pandamods.fallingtrees.entity.TreeEntity;
import me.pandamods.fallingtrees.utils.RenderUtils;
import me.pandamods.joml.Quaternionf;
import me.pandamods.joml.Vector3f;
import me.pandamods.joml.Math;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Environment(EnvType.CLIENT)
public class TreeRenderer extends EntityRenderer<TreeEntity> {
	public TreeRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	public ClientConfig getConfig() {
		return FallingTreesConfig.getClientConfig(Minecraft.getInstance().player);
	}

	@Override
	public void render(TreeEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
		TreeType tree = entity.treeType;
		if (tree == null) return;

		poseStack.pushPose();

		Map<BlockPos, BlockState> blocks = entity.getBlocks();
		float fallAnimLength = getConfig().animation.fallAnimLength;

		float bounceHeight = getConfig().animation.bounceAngleHeight;
		float bounceAnimLength = getConfig().animation.bounceAnimLength;

		float time = (float) (entity.getLifetime(partialTick) * (Math.PI / 2) / fallAnimLength);

		float fallAnim = bumpCos(time) * 90;
		float bounceAnim = bumpSin((float) ((time - Math.PI / 2) / (bounceAnimLength / (fallAnimLength * 2)))) * bounceHeight;

		float animation = (fallAnim + bounceAnim) - 90;

		Direction direction = entity.getDirection().getOpposite();
		float distance = getDistance(tree, blocks, direction.getOpposite());

		Vector3f pivot =  new Vector3f(0, 0, .5f + distance);
		pivot.rotateY(Math.toRadians(-direction.toYRot()));
		poseStack.translate(-pivot.x, 0, -pivot.z);

		Vector3f vector = new Vector3f((float) Math.toRadians(animation), 0, 0);
		vector.rotateY((float) Math.toRadians(-direction.toYRot()));
		Quaternionf quaternion = new Quaternionf().identity().rotateX(vector.x).rotateZ(vector.z);
		poseStack.mulPose(new Quaternion(quaternion.x, quaternion.y, quaternion.z, quaternion.w));

		Level level = entity.getLevel();

		poseStack.translate(pivot.x, 0, pivot.z);
		poseStack.translate(-.5, 0, -.5);
		blocks.forEach((blockPos, blockState) -> {
			poseStack.pushPose();
			poseStack.translate(blockPos.getX(), blockPos.getY(), blockPos.getZ());

			blockPos = blockPos.offset(entity.getOriginPos());
			RenderUtils.renderSingleBlock(poseStack, blockState, blockPos, level, bufferSource, packedLight);

			poseStack.popPose();
		});
		poseStack.popPose();
	}

	private float getDistance(TreeType tree, Map<BlockPos, BlockState> blocks, Direction direction) {
		float distance = 0;
		BlockPos currentPos = new BlockPos(0, 0, 0);
		BlockPos next = currentPos.relative(direction);

		while (blocks.containsKey(next)) {
			if (!tree.isTreeStem(blocks.get(next))) break;

			currentPos = next;
			next = currentPos.relative(direction);

			distance++;
		}
		BlockState blockState = blocks.get(currentPos);
		if (blockState.getOffset(Minecraft.getInstance().level, currentPos).equals(Vec3.ZERO))
			return distance - .5f;

		VoxelShape shape = blockState.getCollisionShape(Minecraft.getInstance().level, currentPos);
		if (shape.isEmpty()) shape = blockState.getShape(Minecraft.getInstance().level, currentPos);

		if (!shape.isEmpty()) {
			AABB bounds = shape.bounds();
			switch (direction) {
				case WEST -> distance -= (float) (bounds.minX);
				case EAST -> distance -= (float) (1f - bounds.maxX);
				case SOUTH -> distance -= (float) (bounds.minZ);
				case NORTH -> distance -= (float) (1f - bounds.maxZ);
			}
		} else {
			distance -= 1;
		}
		return distance;
	}

	private float bumpCos(float time) {
		return (float) Math.max(0, Math.cos(Math.clamp(-Math.PI, Math.PI, time)));
	}

	private float bumpSin(float time) {
		return (float) Math.max(0, Math.sin(Math.clamp(-Math.PI, Math.PI, time)));
	}

	@Override
	public ResourceLocation getTextureLocation(TreeEntity entity) {
		return null;
	}
}
