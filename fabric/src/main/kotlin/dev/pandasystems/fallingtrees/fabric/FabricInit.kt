/*
 * Copyright (C) 2025-2026 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package dev.pandasystems.fallingtrees.fabric

import dev.pandasystems.fallingtrees.core.FallingTreesMain
import net.fabricmc.api.ModInitializer

internal class FabricInit : ModInitializer {
	override fun onInitialize() {
		FallingTreesMain()
	}
}
