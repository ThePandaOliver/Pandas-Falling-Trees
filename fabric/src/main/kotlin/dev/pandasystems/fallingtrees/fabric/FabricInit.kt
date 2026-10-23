package dev.pandasystems.fallingtrees.fabric

import dev.pandasystems.fallingtrees.FallingTreesMain
import net.fabricmc.api.ModInitializer

internal class FabricInit : ModInitializer {
	override fun onInitialize() {
		FallingTreesMain()
	}
}
