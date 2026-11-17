package dev.pandasystems.fallingtrees.neoforge

import dev.pandasystems.fallingtrees.FallingTreesMain
import dev.pandasystems.fallingtrees.modId
import net.neoforged.fml.common.Mod

@Mod(modId)
internal class NeoForgeInit {
	init {
		FallingTreesMain()
	}
}
