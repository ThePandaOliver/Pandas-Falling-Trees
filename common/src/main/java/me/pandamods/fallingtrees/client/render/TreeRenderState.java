package me.pandamods.fallingtrees.client.render;

import me.pandamods.fallingtrees.api.Tree;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;

public class TreeRenderState extends EntityRenderState {
	public Tree<?> tree;
	public Map<BlockPos, BlockState> blocks;
	public double lifeTime;
	public Direction direction;
	public Level level;
	public Vec3i originPos;
}
