package dev.pandasystems.fallingtrees.config

import dev.pandasystems.pandalib.config.handle.ConfigHandle
import kotlinx.serialization.Serializable

@Serializable
data class CommonConfig(
	var disableCrouchMining: Boolean = false,
	var disableExtraToolDamage: Boolean = false,
	var disableExtraFoodExhaustion: Boolean = false
) {
	companion object {
		lateinit var HANDLE: ConfigHandle<CommonConfig>
			internal set
	}
}