package dev.pandasystems.fallingtrees.client.core

import dev.pandasystems.fallingtrees.client.rendering.TreeRenderer
import dev.pandasystems.fallingtrees.core.FallingTreesRegistries
import dev.pandasystems.pandalib.client.rendering.RendererRegistry


class FallingTreesClientMain {
	init {
		instance = this

		RendererRegistry.registerEntityRenderer(FallingTreesRegistries.treeEntity, ::TreeRenderer)
	}

	companion object {
		lateinit var instance: FallingTreesClientMain
			private set
	}
}