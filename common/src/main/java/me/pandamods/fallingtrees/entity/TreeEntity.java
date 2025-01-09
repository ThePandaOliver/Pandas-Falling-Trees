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

package me.pandamods.fallingtrees.entity;

import me.pandamods.fallingtrees.api.Tree;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import me.pandamods.fallingtrees.registry.EntityRegistry;
import me.pandamods.fallingtrees.registry.TreeRegistry;
import me.pandamods.fallingtrees.utils.BlockMapEntityData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TreeEntity extends Entity {
	public static final EntityDataAccessor<Map<BlockPos, BlockState>> BLOCKS = SynchedEntityData.defineId(TreeEntity.class, BlockMapEntityData.BLOCK_MAP);
	public static final EntityDataAccessor<Integer> HEIGHT = SynchedEntityData.defineId(TreeEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<BlockPos> ORIGIN_POS = SynchedEntityData.defineId(TreeEntity.class, EntityDataSerializers.BLOCK_POS);
	public static final EntityDataAccessor<ItemStack> USED_TOOL = SynchedEntityData.defineId(TreeEntity.class, EntityDataSerializers.ITEM_STACK);
	public static final EntityDataAccessor<Direction> FALL_DIRECTION = SynchedEntityData.defineId(TreeEntity.class, EntityDataSerializers.DIRECTION);
	public static final EntityDataAccessor<String> TREE_TYPE_LOCATION = SynchedEntityData.defineId(TreeEntity.class, EntityDataSerializers.STRING);

	public Entity owner = null;
	public Tree<?> tree = null;

	public TreeEntity(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	public void setData(List<BlockPos> blockPosList, BlockPos originBlock, Tree<?> tree, Entity owner, ItemStack itemStack) {
		this.owner = owner;
		this.tree = tree;

		int height = 0;

		Level level = level();

		Map<BlockPos, BlockState> blockPosMap = new HashMap<>();
		for (BlockPos pos : blockPosList) {
			if (pos.getY() > height)
				height = pos.getY() - originBlock.getY();
			blockPosMap.put(pos.immutable().subtract(originBlock), level.getBlockState(pos));
		}
		this.getEntityData().set(ORIGIN_POS, originBlock);
		this.getEntityData().set(BLOCKS, blockPosMap);
		this.getEntityData().set(HEIGHT, height);
		this.getEntityData().set(USED_TOOL, itemStack);
		ResourceLocation treeTypeLocation = TreeRegistry.getTreeLocation(tree);
		if (treeTypeLocation != null)
			this.getEntityData().set(TREE_TYPE_LOCATION, treeTypeLocation.toString());
		this.getEntityData().set(FALL_DIRECTION, Direction.fromYRot(
				-Math.toDegrees(Math.atan2(owner.getX() - originBlock.getX(), owner.getZ() - originBlock.getZ()))
		).getOpposite());
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(BLOCKS, new HashMap<>());
		builder.define(HEIGHT, 0);
		builder.define(ORIGIN_POS, new BlockPos(0, 0, 0));
		builder.define(USED_TOOL, ItemStack.EMPTY);
		builder.define(FALL_DIRECTION, Direction.NORTH);
		builder.define(TREE_TYPE_LOCATION, "");
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag compound) {}

	@Override
	protected void addAdditionalSaveData(CompoundTag compound) {}

	@Override
	public void tick() {
		super.tick();

		if (!this.isNoGravity()) {
			this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
		}
		this.move(MoverType.SELF, this.getDeltaMovement());
		if (this.onGround()) {
			this.setDeltaMovement(this.getDeltaMovement().multiply(1, -0.5, 1));
		}

		this.getTree().entityTick(this);
	}

	@Override
	public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
		return false;
	}

	public Map<BlockPos, BlockState> getBlocks() {
		return this.getEntityData().get(BLOCKS);
	}

	public int getMaxLifeTimeTick() {
		return (int) (FallingTreesConfig.getCommonConfig().treeLifetimeLength * 20);
	}

	public float getLifetime(float partialTick) {
		return (this.tickCount + partialTick) / 20;
	}

	public float getHeight() {
		return this.getEntityData().get(HEIGHT);
	}

	public BlockPos getOriginPos() {
		return this.getEntityData().get(ORIGIN_POS);
	}

	public ItemStack getUsedTool() {
		return this.getEntityData().get(USED_TOOL);
	}

	public @NotNull Direction getDirection() {
		return this.getEntityData().get(FALL_DIRECTION);
	}

	public Tree<?> getTree() {
		return TreeRegistry.getTree(ResourceLocation.tryParse(this.getEntityData().get(TREE_TYPE_LOCATION)));
	}
}
