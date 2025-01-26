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

package me.pandamods.fallingtrees.event;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.utils.value.IntValue;
import me.pandamods.fallingtrees.api.TreeHandler;
import me.pandamods.fallingtrees.compat.TreeChopCompat;
import me.pandamods.fallingtrees.config.FallingTreesConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class EventHandler {
	public static void register() {
		BlockEvent.BREAK.register(EventHandler::onBlockBreak);
	}

	private static EventResult onBlockBreak(Level level, BlockPos blockPos, BlockState blockState, ServerPlayer serverPlayer, IntValue intValue) {
		if (serverPlayer == null)
			return EventResult.pass();
		
		if (!TreeHandler.canPlayerChopTree(serverPlayer))
			return EventResult.pass();
		
		if (!TreeChopCompat.isChoppable(level, blockPos) && TreeHandler.destroyTree(level, blockPos, serverPlayer))
			return EventResult.interruptFalse();
		
		return EventResult.pass();
	}
}
