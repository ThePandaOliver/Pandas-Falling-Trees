package dev.pandasystems.fallingtrees.core

import dev.pandasystems.fallingtrees.config.CommonConfig
import dev.pandasystems.pandalib.config.ConfigManager
import dev.pandasystems.pandalib.config.store.InMemoryConfigStore


class FallingTreesMain {
	init {
		install(this)

		CommonConfig.HANDLE = ConfigManager.load(
			InMemoryConfigStore(),
			{ CommonConfig() }
		)
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