package me.pandamods.fallingtrees.api;

import com.mojang.logging.LogUtils;
import me.pandamods.fallingtrees.entity.TreeEntity;
import me.pandamods.fallingtrees.exceptions.TreeException;
import me.pandamods.fallingtrees.registry.EntityRegistry;
import me.pandamods.fallingtrees.registry.TreeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.*;

public class TreeHandler {
	private static final Logger LOGGER = LogUtils.getLogger();
	
	public static boolean destroyTree(Level level, BlockPos blockPos, Player player) {
		if (level.isClientSide()) return false;
		BlockState blockState = level.getBlockState(blockPos);
		
		Tree<?> tree = TreeRegistry.getTree(blockState);
		if (tree == null || !tree.enabled()) return false;
		
		try {
			TreeData data = tree.gatherTreeData(blockPos, level);
			if (data == null) return false;
			List<BlockPos> blocks = data.blocks();
			
			TreeEntity entity = new TreeEntity(EntityRegistry.TREE.get(), level);
			entity.setPos(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
			entity.setData(data.blocks(), blockPos, tree, player, player.getItemBySlot(EquipmentSlot.MAINHAND));

			for (BlockPos block : blocks) {
				level.removeBlock(block, false);
			}
			level.addFreshEntity(entity);
			return true;
		} catch (TreeException e) {
			LOGGER.warn(e.getMessage());
		}
		return false;
	}
}
