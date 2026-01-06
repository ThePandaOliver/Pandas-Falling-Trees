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

import dev.pandasystems.fallingtrees.config.fallingTreesCommonConfig
import dev.pandasystems.fallingtrees.getTree
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.MultiPlayerGameMode
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.player.Player

object MultiPlayerGameModeKtImpl {
	var lastTickCrouchState = false
	var blockDestroyDirection = Direction.UP

	fun startDestroyBlock(face: Direction) {
		blockDestroyDirection = face
	}

	fun tick(minecraft: Minecraft, destroyBlockPos: BlockPos, gameMode: MultiPlayerGameMode) {
		if (fallingTreesCommonConfig.get().dynamicMiningSpeed.disable) return
		val player: Player? = minecraft.player

		if (player != null) {
			val level = player.level()

			val blockState = level.getBlockState(destroyBlockPos)
			if (getTree(blockState) != null) {
				if (player.isCrouching != lastTickCrouchState) {
					if (gameMode.isDestroying && minecraft.gameMode != null) {
						gameMode.stopDestroyBlock()
						gameMode.startDestroyBlock(destroyBlockPos, blockDestroyDirection)
					}
				}
				this.lastTickCrouchState = player.isCrouching
			}
		}
	}
}