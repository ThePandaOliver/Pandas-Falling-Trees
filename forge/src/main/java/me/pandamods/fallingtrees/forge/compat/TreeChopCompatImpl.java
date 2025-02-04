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

package me.pandamods.fallingtrees.forge.compat;

import ht.treechop.api.ChopEvent;
import ht.treechop.common.registry.ForgeModBlocks;
import me.pandamods.fallingtrees.compat.TreeChopCompat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;

public class TreeChopCompatImpl implements TreeChopCompat {
	public TreeChopCompatImpl() {
		MinecraftForge.EVENT_BUS.addListener(TreeChopCompatImpl::beforeFellEvent);
	}

	public static void beforeFellEvent(final ChopEvent.BeforeFellEvent event) {
		if (event.getPlayer() instanceof ServerPlayer serverPlayer)
			event.setCanceled(!TreeChopCompat.beforeFellEvent(event.getLevel(), serverPlayer, event.getChoppedBlockPos(), event.getFellData()));
	}

	public boolean isCoppedLog(BlockState blockState) {
		return blockState.is(ForgeModBlocks.CHOPPED_LOG.get());
	}
}
