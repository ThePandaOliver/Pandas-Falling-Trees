package me.pandamods.fallingtrees.api;

import com.mojang.logging.LogUtils;
import dev.architectury.event.EventResult;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import me.pandamods.fallingtrees.entity.TreeEntity;
import me.pandamods.fallingtrees.exceptions.TreeException;
import me.pandamods.fallingtrees.registry.EntityRegistry;
import me.pandamods.fallingtrees.registry.TreeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TreeHandler {
	private static final Logger LOGGER = LogUtils.getLogger();

	public  static final Map<UUID, TreeSpeed> TREE_SPEED_CACHES = new ConcurrentHashMap<>();
	
	public static boolean destroyTree(Level level, BlockPos blockPos, Player player) {
		if (level.isClientSide()) return false;
		BlockState blockState = level.getBlockState(blockPos);
		
		TreeType tree = TreeRegistry.getTree(blockState);
		if (tree == null) return false;
		
		try {
			TreeData data = tree.gatherTreeData(blockPos, level);
			if (data == null) return false;
			List<BlockPos> blocks = data.blocks();

			TreeEntity entity = new TreeEntity(EntityRegistry.TREE.get(), level);
			entity.setPos(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
			entity.setData(player, tree, blocks, blockPos);

//			for (BlockPos block : blocks) {
//				level.removeBlock(block, false);
//			}
			level.addFreshEntity(entity);
			return true;
		} catch (TreeException e) {
			LOGGER.warn(e.getMessage());
		}
		return false;
	}
	
	public static boolean canPlayerChopTree(Player player) {
		return FallingTreesConfig.getCommonConfig().disableCrouchMining || !player.isCrouching();
	}
	
	public static Optional<Float> getMiningSpeed(Player player, BlockPos blockPos, float baseSpeed) {
		TreeSpeed treeSpeed = TREE_SPEED_CACHES.compute(player.getUUID(), (uuid, speed) -> {
			if (speed == null || speed.blockPos != blockPos) {
				BlockState blockState = player.level().getBlockState(blockPos);
				TreeType tree = TreeRegistry.getTree(blockState);
				if (tree == null) return null;
				TreeData data = tree.gatherTreeData(blockPos, player.level());
				if (data == null) return null;
				return new TreeSpeed(data.miningSpeedModifier().getMiningSpeed(blockState, baseSpeed), blockPos.immutable());
			}
			return speed;
		});
		return Optional.ofNullable(treeSpeed).map(TreeSpeed::miningSpeed);
	}

	public record TreeSpeed(float miningSpeed, BlockPos blockPos) {}
}
