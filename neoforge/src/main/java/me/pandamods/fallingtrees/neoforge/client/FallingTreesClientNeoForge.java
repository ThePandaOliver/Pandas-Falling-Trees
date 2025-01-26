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

package me.pandamods.fallingtrees.neoforge.client;

import me.pandamods.fallingtrees.FallingTrees;
import me.pandamods.fallingtrees.client.FallingTreesClient;
import me.pandamods.fallingtrees.client.render.TreeRenderer;
import me.pandamods.fallingtrees.registry.EntityRegistry;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@Mod(value = FallingTrees.MOD_ID, dist = Dist.CLIENT)
public class FallingTreesClientNeoForge {
	public FallingTreesClientNeoForge(IEventBus modBus) {
		FallingTreesClient.init();

		modBus.addListener(FallingTreesClientNeoForge::registerRenderers);
	}

	public static void registerRenderers(final EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(EntityRegistry.TREE.get(), TreeRenderer::new);
	}
}