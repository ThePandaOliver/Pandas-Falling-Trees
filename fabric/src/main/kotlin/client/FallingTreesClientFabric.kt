/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU General Public License v3.0
 * See: https://www.gnu.org/licenses/gpl-3.0-standalone.html
 */
package dev.pandasystems.fallingtrees.fabric.client

import dev.pandasystems.fallingtrees.client.FallingTreesClient
import net.fabricmc.api.ClientModInitializer

class FallingTreesClientFabric : ClientModInitializer {
	override fun onInitializeClient() {
		FallingTreesClient
	}
}
