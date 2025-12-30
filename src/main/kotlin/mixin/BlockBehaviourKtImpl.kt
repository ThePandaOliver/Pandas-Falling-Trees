package dev.pandasystems.fallingtrees.mixin

import dev.pandasystems.fallingtrees.api.TreeHandler
import dev.pandasystems.fallingtrees.api.TreeHandler.canPlayerChopTree
import dev.pandasystems.fallingtrees.config.fallingTreesCommonConfig
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.function.Consumer

class BlockBehaviourKtImpl {
	fun getDestroyProgress(player: Player, pos: BlockPos, cir: CallbackInfoReturnable<Float>) {
		if (fallingTreesCommonConfig.get().dynamicMiningSpeed.disable) return
		if (!canPlayerChopTree(player)) return
		val miningSpeedOpt = TreeHandler.getMiningSpeed(player, pos, cir.getReturnValue())
		miningSpeedOpt.ifPresent(Consumer { returnValue: Float -> cir.returnValue = returnValue })
	}
}