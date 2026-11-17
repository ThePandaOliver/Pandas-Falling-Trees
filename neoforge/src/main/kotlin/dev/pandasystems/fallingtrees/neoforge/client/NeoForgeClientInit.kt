package dev.pandasystems.fallingtrees.neoforge.client

import dev.pandasystems.fallingtrees.client.FallingTreesClientMain
import dev.pandasystems.fallingtrees.modId
import net.neoforged.api.distmarker.Dist
import net.neoforged.fml.common.Mod

@Mod(value = modId, dist = [Dist.CLIENT])
internal class NeoForgeClientInit {
	init {
		FallingTreesClientMain()
	}
}
