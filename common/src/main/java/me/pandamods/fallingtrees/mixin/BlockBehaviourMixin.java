package me.pandamods.fallingtrees.mixin;

import me.pandamods.fallingtrees.api.TreeHandler;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {
	@Inject(method = "getDestroyProgress", at = @At("RETURN"), cancellable = true)
	private void getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		if (FallingTreesConfig.getCommonConfig().dynamicMiningSpeed.disable) return;
		if (player == null || !TreeHandler.canPlayerChopTree(player)) return;
		Optional<Float> miningSpeedOpt = TreeHandler.getMiningSpeed(player, pos, cir.getReturnValue());
		miningSpeedOpt.ifPresent(cir::setReturnValue);
	}
}
