package dev.pandasystems.fallingtrees.mixin;

import dev.pandasystems.fallingtrees.tree.TreeFeller;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {
	@Inject(method = "getDestroyProgress", at = @At("RETURN"), cancellable = true)
	private void fallingtrees$scaleTreeDestroyProgress(
		BlockState state,
		Player player,
		BlockGetter level,
		BlockPos pos,
		CallbackInfoReturnable<Float> cir
	) {
		float baseProgress = cir.getReturnValueF();
		float modified = TreeFeller.modifyDestroyProgress(baseProgress, state, player, level, pos);
		if (modified != baseProgress) {
			cir.setReturnValue(modified);
		}
	}
}
