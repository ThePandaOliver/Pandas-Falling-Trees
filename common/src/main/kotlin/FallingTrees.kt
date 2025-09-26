/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU General Public License v3.0
 * See: https://www.gnu.org/licenses/gpl-3.0-standalone.html
 */
package dev.pandasystems.fallingtrees

import dev.pandasystems.fallingtrees.config.registerPlayerConfigPayload
import dev.pandasystems.fallingtrees.event.EventHandler
import net.minecraft.resources.ResourceLocation

object FallingTrees {
	const val MOD_ID: String = "fallingtrees"

	init {
		treeRegister.register()
		soundRegister.register()
		entityRegistar.register()
		entityDataRegistar.register()
		EventHandler.register()

		registerPlayerConfigPayload()
	}

	@JvmStatic
	fun resourceLocation(path: String): ResourceLocation {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path)
	}
}
