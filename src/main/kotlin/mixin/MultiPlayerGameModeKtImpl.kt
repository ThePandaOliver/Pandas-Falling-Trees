package dev.pandasystems.fallingtrees.mixin

import dev.pandasystems.fallingtrees.config.fallingTreesCommonConfig
import dev.pandasystems.fallingtrees.getTree
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.MultiPlayerGameMode
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.player.Player

class MultiPlayerGameModeKtImpl {
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