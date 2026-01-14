/*
 * Copyright (C) 2026 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package dev.pandasystems.fallingtrees

import dev.pandasystems.fallingtrees.api.TreeHandler
import dev.pandasystems.fallingtrees.config.initConfigs
import dev.pandasystems.pandalib.PandaLib.modid
import net.minecraft.resources.Identifier

object FallingTrees {
	const val MODID: String = "fallingtrees"

	init {
		initConfigs()

		treeRegister.register()
		soundRegister.register()
		entityRegister.register()
		entityDataRegister.register()
		TreeHandler.init()
	}

	@Deprecated("ResourceLocation renamed to Identifier", ReplaceWith("identifier(path)"))
	fun resourceLocation(path: String): Identifier = Identifier.fromNamespaceAndPath(modid, path)
	fun identifier(path: String): Identifier = Identifier.fromNamespaceAndPath(modid, path)
}
