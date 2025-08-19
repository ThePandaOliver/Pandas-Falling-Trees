/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU General Public License v3.0
 * See: https://www.gnu.org/licenses/gpl-3.0-standalone.html
 */
package dev.pandasystems.fallingtrees.neoforge

import dev.pandasystems.fallingtrees.FallingTrees
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.common.Mod

@Mod(FallingTrees.MOD_ID)
class FallingTreesNeoForge(eventBus: IEventBus) {
	init {
		FallingTrees
	}
}
