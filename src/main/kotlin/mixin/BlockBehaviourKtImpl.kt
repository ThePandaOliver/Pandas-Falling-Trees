/*
 * Copyright (C) 2026 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.pandasystems.fallingtrees.mixin

import dev.pandasystems.fallingtrees.api.TreeHandler
import dev.pandasystems.fallingtrees.api.TreeHandler.canPlayerChopTree
import dev.pandasystems.fallingtrees.config.fallingTreesCommonConfig
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import java.util.function.Consumer

object BlockBehaviourKtImpl {
	fun getDestroyProgress(player: Player, pos: BlockPos, cir: CallbackInfoReturnable<Float>) {
		if (fallingTreesCommonConfig.get().dynamicMiningSpeed.disable) return
		if (!canPlayerChopTree(player)) return
		val miningSpeedOpt = TreeHandler.getMiningSpeed(player, pos, cir.getReturnValue())
		miningSpeedOpt.ifPresent(Consumer { returnValue: Float -> cir.returnValue = returnValue })
	}
}