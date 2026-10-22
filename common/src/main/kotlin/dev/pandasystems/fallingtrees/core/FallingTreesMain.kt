package dev.pandasystems.fallingtrees.core

import dev.pandasystems.fallingtrees.config.initConfigs


class FallingTreesMain {
	init {
		instance = this
		initConfigs()
		FallingTreesRegistries.register()
	}

	companion object {
		lateinit var instance: FallingTreesMain
			private set
	}
}