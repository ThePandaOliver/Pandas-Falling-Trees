/*
 * Copyright (C) 2024 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.pandamods.fallingtrees.config;

import me.pandamods.fallingtrees.FallingTrees;
import me.pandamods.fallingtrees.config.common.TreeConfigs;
import dev.pandasystems.pandalib.config.Config;
import dev.pandasystems.pandalib.config.ConfigData;

@Config(name = FallingTrees.MOD_ID + "_common", modId = FallingTrees.MOD_ID, synchronize = true)
public class CommonConfig implements ConfigData {
	public boolean disableCrouchMining = false;
	public boolean disableExtraToolDamage = false;
	public boolean disableExtraFoodExhaustion = false;

	public float treeLifetimeLength = 4;

	public DynamicMiningSpeed dynamicMiningSpeed = new DynamicMiningSpeed();
	public TreeConfigs trees = new TreeConfigs();

	public static class DynamicMiningSpeed {
		public boolean disable = false;
		public float speedMultiplication = 0.5f;
		public float maxSpeedMultiplication = 16f;
	}
}
