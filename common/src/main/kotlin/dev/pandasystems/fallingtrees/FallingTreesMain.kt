package dev.pandasystems.fallingtrees

import dev.pandasystems.fallingtrees.config.initConfigs
import dev.pandasystems.fallingtrees.tree.TreeFeller

class FallingTreesMain {
	init {
		instance = this
		initConfigs()
		FallingTreesRegistries.register()
		TreeFeller.init()
	}

	companion object {
		lateinit var instance: FallingTreesMain
			private set
	}
}
