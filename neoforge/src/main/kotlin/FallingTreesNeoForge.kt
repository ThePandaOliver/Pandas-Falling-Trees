/*
 * Copyright (C) 2025-2025 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package dev.pandasystems.fallingtrees.neoforge

import dev.pandasystems.fallingtrees.FallingTrees
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod

@Mod(FallingTrees.modid)
class FallingTreesNeoForge(eventBus: IEventBus) {
	init {
		FallingTrees
	}
}
