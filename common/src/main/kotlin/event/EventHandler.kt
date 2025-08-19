/*
 * Copyright (C) 2024 Oliver Froberg (The Panda Oliver)
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
import dev.pandasystems.pandalib.api.event.addEventListener
import dev.pandasystems.pandalib.api.event.commonevents.BlockBreakEvent
import net.minecraft.server.level.ServerPlayer

object EventHandler {
	fun register() {
		addEventListener(::onBlockBreak)
	}

	private fun onBlockBreak(event: BlockBreakEvent.Pre) {
		if (event.entity !is ServerPlayer) return
		val player = event.entity as ServerPlayer

		if (!TreeHandler.canPlayerChopTree(player))
			return

		if (TreeHandler.destroyTree(event.level, event.blockPos, player)) {
			event.cancelled = true
			return
		}
	}
}
