package dev.pandasystems.fallingtrees.config

import kotlinx.serialization.Serializable

@Serializable
data class CommonConfig(
	// Activation
	var disableCrouchMining: Boolean = false,
	var reverseCrouchMining: Boolean = false,
	var requireAxe: Boolean = false,
	var customAllowedTools: List<String> = emptyList(),

	// Costs
	var disableExtraToolDamage: Boolean = false,
	var preserveToolBeforeBreak: Boolean = true,
	var disableExtraFoodExhaustion: Boolean = false,
	var exhaustionPerBlock: Float = 0.005f,

	// Rules
	var requireFullGroundSeparation: Boolean = true,
	var scanBelowOrigin: Boolean = false,
	var maxTrunkBlocks: Int = 256,
	var maxLeafBlocks: Int = 512,
	var maxLeafDistance: Int = 6,
	var minLeavesRequired: Int = 3,
	var breakLeavesInstantly: Boolean = true,

	// Dynamic Mining Speed
	var scaleMiningSpeedWithSize: Boolean = true,
	var miningSpeedPenaltyPerBlock: Float = 0.15f,
	var miningSpeedExponent: Float = 0.85f,
	var maxMiningTimeMultiplier: Float = 12.0f,

	// Built-in Special Flora Support
	var enableCactus: Boolean = true,
	var enableBamboo: Boolean = true,
	var enableGiantMushrooms: Boolean = true,
	var enableNetherFungi: Boolean = true,
	var enableChorusPlants: Boolean = false,

	// Custom Block & Tag Extensions
	var extraLogBlocks: List<String> = emptyList(),
	var extraLeafBlocks: List<String> = emptyList(),
	var blacklistedBlocks: List<String> = emptyList()
)
