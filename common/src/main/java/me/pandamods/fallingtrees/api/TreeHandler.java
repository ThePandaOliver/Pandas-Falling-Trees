package me.pandamods.fallingtrees.api;

import com.mojang.logging.LogUtils;
import me.pandamods.fallingtrees.config.ClientConfig;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import me.pandamods.fallingtrees.entity.TreeEntity;
import me.pandamods.fallingtrees.exceptions.TreeException;
import me.pandamods.fallingtrees.registry.EntityRegistry;
import me.pandamods.fallingtrees.registry.TreeRegistry;
import me.pandamods.pandalib.utils.RunUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TreeHandler {
	private static final Logger LOGGER = LogUtils.getLogger();

	public  static final Map<UUID, TreeSpeed> TREE_SPEED_CACHES = new ConcurrentHashMap<>();
	
	public static boolean destroyTree(Level level, BlockPos blockPos, Player player) {
		if (level.isClientSide()) return false;
		BlockState blockState = level.getBlockState(blockPos);
		
		TreeType tree = TreeRegistry.getTree(blockState);
		if (tree == null) return false;

		TreeData data = tryGatherTreeData(tree, blockPos, level, player, false);
		if (data == null) return false;
		List<BlockPos> blocks = data.blocks();

		TreeEntity entity = new TreeEntity(EntityRegistry.TREE.get(), level);
		entity.setPos(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
		entity.setData(player, tree, blockPos, blocks, data.drops());

		player.causeFoodExhaustion(
				FallingTreesConfig.getCommonConfig().disableExtraFoodExhaustion ? 1 :
						data.foodExhaustionModifier().getExhaustion(0.005F)
		);

		if (player.getMainHandItem().isDamageableItem())
			player.getMainHandItem().hurtAndBreak(
					FallingTreesConfig.getCommonConfig().disableExtraToolDamage ? 1 : data.toolDamage(),
					player, player1 -> player1.broadcastBreakEvent(EquipmentSlot.MAINHAND)
			);

		player.awardStat(Stats.ITEM_USED.get(player.getMainHandItem().getItem()));
		data.awardedStats().forEach(awardedStat -> player.awardStat(awardedStat.stat(), awardedStat.amount()));

		Map<BlockPos, BlockState> blockStates = new HashMap<>();

		// Silently remove all blocks
		BlockState air = Blocks.AIR.defaultBlockState();
		for (BlockPos pos : blocks) {
			BlockState oldState = level.getBlockState(pos);
			level.setBlock(pos, air, 16);
			level.setBlocksDirty(pos, oldState, level.getBlockState(pos));
			blockStates.put(pos, oldState);
		}

		// Update neighbors around removed blocks
		blockStates.forEach((pos, oldState) -> {
			BlockState newState = level.getBlockState(pos);

			level.sendBlockUpdated(pos, oldState, newState, 3);
			level.sendBlockUpdated(pos, oldState, newState, 3);
			level.blockUpdated(pos, newState.getBlock());

			newState.updateIndirectNeighbourShapes(level, pos, 511);
			oldState.updateNeighbourShapes(level, pos, 511);
			oldState.updateIndirectNeighbourShapes(level, pos, 511);

			level.onBlockStateChange(pos, oldState, newState);
		});
		level.addFreshEntity(entity);
		return true;
	}

	public static TreeData tryGatherTreeData(TreeType treeType, BlockPos blockPos, Level level, Player player, boolean ignoreExceptions) {
		try {
			return treeType.gatherTreeData(blockPos, level, player);
		} catch (TreeException e) {
			if (!ignoreExceptions) {
				LOGGER.warn(e.getMessage());
			}
		} catch (Exception e) {
			if (!ignoreExceptions) {
				LOGGER.error("An error occurred when trying to gather tree data", e);
				player.displayClientMessage(Component.literal("Error: " + e).withStyle(Style.EMPTY.withColor(Color.red.getRGB())), false);
				player.displayClientMessage(Component.translatable("text.fallingtrees.tree_handler.exception.1").withStyle(Style.EMPTY.withColor(Color.red.getRGB())), false);
				player.displayClientMessage(Component.translatable("text.fallingtrees.tree_handler.exception.2").withStyle(Style.EMPTY.withColor(Color.red.getRGB())), false);
			}
		}
		return null;
	}
	
	public static boolean canPlayerChopTree(Player player) {
		ClientConfig clientConfig = FallingTreesConfig.getClientConfig(player);
		if (clientConfig == null) {
			RunUtil.runOnce("falling_trees_player_client_config_missing_" + player.getUUID(), () -> LOGGER.warn("Couldn't find client config for player: {} [{}]", player.getDisplayName().getString(), player.getUUID()));
			return false;
		}
		boolean invertCrouchMining = clientConfig.invertCrouchMining;
		return FallingTreesConfig.getCommonConfig().disableCrouchMining || player.isCrouching() == invertCrouchMining;
	}
	
	public static Optional<Float> getMiningSpeed(Player player, BlockPos blockPos, float baseSpeed) {
		TreeSpeed treeSpeed = TREE_SPEED_CACHES.compute(player.getUUID(), (uuid, speed) -> {
			if (speed == null || !speed.isValid(blockPos, baseSpeed)) {
				BlockState blockState = player.level().getBlockState(blockPos);
				TreeType tree = TreeRegistry.getTree(blockState);
				if (tree == null) return null;
				TreeData data = tryGatherTreeData(tree, blockPos, player.level(), player, true);
				if (data == null) return null;
				return new TreeSpeed(baseSpeed, data.miningSpeedModifier().getMiningSpeed(baseSpeed), blockPos.immutable());
			}
			return speed;
		});
		return Optional.ofNullable(treeSpeed).map(TreeSpeed::getMiningSpeed);
	}

	public static final class TreeSpeed {
		private final float baseMiningSpeed;
		private final float miningSpeed;
		private final BlockPos blockPos;

		public TreeSpeed(float baseMiningSpeed, float miningSpeed, BlockPos blockPos) {
			this.baseMiningSpeed = baseMiningSpeed;
			this.miningSpeed = miningSpeed;
			this.blockPos = blockPos;
		}

		public float getMiningSpeed() {
			return miningSpeed;
		}

		public boolean isValid(BlockPos blockPos, float baseSpeed) {
			return Objects.equals(this.blockPos, blockPos) && this.baseMiningSpeed == baseSpeed;
		}
	}
}
