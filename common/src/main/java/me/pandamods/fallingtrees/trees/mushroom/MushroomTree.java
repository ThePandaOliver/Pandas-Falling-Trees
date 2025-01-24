package me.pandamods.fallingtrees.trees.mushroom;

import me.pandamods.fallingtrees.api.TreeData;
import me.pandamods.fallingtrees.api.TreeType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class MushroomTree implements TreeType {
	public static final RedMushroomTree RED_MUSHROOM_TREE = new RedMushroomTree();
	public static final BrownMushroomTree BROWN_MUSHROOM_TREE = new BrownMushroomTree();

	@Override
	public boolean isTreeStem(BlockState blockState) {
		return blockState.is(Blocks.MUSHROOM_STEM);
	}

	@Override
	public TreeData gatherTreeData(BlockPos blockPos, Level level, Player player) {
		TreeData data = RED_MUSHROOM_TREE.gatherTreeData(blockPos, level, player);
		if (data == null)
			data = BROWN_MUSHROOM_TREE.gatherTreeData(blockPos, level, player);
		return data;
	}
}
