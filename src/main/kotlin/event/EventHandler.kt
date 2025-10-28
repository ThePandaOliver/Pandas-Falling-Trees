/*
 * Copyright (C) 2025 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.pandasystems.fallingtrees.event

import dev.pandasystems.fallingtrees.api.TreeHandler
import dev.pandasystems.pandalib.event.server.serverBlockBreakPreEvent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

object EventHandler {
	fun register() {
		serverBlockBreakPreEvent += ::onBlockBreak
	}

	private fun onBlockBreak(level: Level, pos: BlockPos, state: BlockState, entity: Entity?): Boolean {
		if (entity is ServerPlayer) {
			if (!TreeHandler.canPlayerChopTree(entity))
				return true

			if (TreeHandler.destroyTree(level, pos, entity)) {
				return false
			}
		}

		return true
	}
}
