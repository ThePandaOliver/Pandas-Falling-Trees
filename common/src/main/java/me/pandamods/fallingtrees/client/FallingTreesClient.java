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

package me.pandamods.fallingtrees.client;

import dev.architectury.platform.Platform;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import me.pandamods.fallingtrees.client.render.TreeRenderer;
import me.pandamods.fallingtrees.registry.EntityRegistry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class FallingTreesClient {
    public static void init() {
		if (!Platform.isNeoForge()) {
			EntityRendererRegistry.register(EntityRegistry.TREE, TreeRenderer::new);
		}
    }
}
