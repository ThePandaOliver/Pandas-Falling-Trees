package dev.pandasystems.fallingtrees.fabric.client

import dev.pandasystems.dev.pandasystems.fallingtrees.core.client.FallingTreesClientMain
import net.fabricmc.api.ClientModInitializer

internal class FabricClientInit : ClientModInitializer {
	override fun onInitializeClient() {
		FallingTreesClientMain()
	}
}
