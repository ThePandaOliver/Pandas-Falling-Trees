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

package dev.pandasystems.fallingtrees.client

import dev.pandasystems.fallingtrees.client.render.TreeRenderer
import dev.pandasystems.fallingtrees.treeEntity
import dev.pandasystems.pandalib.api.registry.registerEntityRenderer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment

@Environment(EnvType.CLIENT)
object FallingTreesClient {
	init {
		registerEntityRenderer(treeEntity.get(), ::TreeRenderer)
	}
}
