package dev.pandasystems.fallingtrees.core

import dev.pandasystems.fallingtrees.config.CommonConfig
import dev.pandasystems.fallingtrees.config.initConfigs
import dev.pandasystems.pandalib.config.ConfigManager
import dev.pandasystems.pandalib.config.store.InMemoryConfigStore


class FallingTreesMain {
	init {
		install(this)
		initConfigs()
	}

	companion object {
		lateinit var instance: FallingTreesMain
			private set

		private fun install(main: FallingTreesMain) {
			if (!::instance.isInitialized) {
				instance = main
			} else {
				logger.warn("""
					PFallingTreesMain instance already initialized.
					This is only intended for testing purposes.
				""".trimIndent())
			}
		}
	}
}