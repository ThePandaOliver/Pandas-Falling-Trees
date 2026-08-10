package dev.pandasystems.fallingtrees.core


class FallingTreesMain() {
	init {
		install(this)
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