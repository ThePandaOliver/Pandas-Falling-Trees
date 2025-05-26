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
import dev.pandasystems.pandalib.config.PandaLibConfig;
import dev.pandasystems.pandalib.config.holders.ClientConfigHolder;
import dev.pandasystems.pandalib.config.holders.CommonConfigHolder;
import net.minecraft.world.entity.player.Player;

public class FallingTreesConfig {
	public final ClientConfigHolder<ClientConfig> clientConfigHolder = PandaLibConfig.registerClient(ClientConfig.class);
	public final CommonConfigHolder<CommonConfig> commonConfigHolder = PandaLibConfig.registerCommon(CommonConfig.class);

	public static ClientConfig getClientConfig(Player player) {
		return FallingTrees.CONFIG.clientConfigHolder.getConfig(player);
	}

	public static ClientConfig getClientConfig() {
		return FallingTrees.CONFIG.clientConfigHolder.get();
	}

	public static CommonConfig getCommonConfig() {
		return FallingTrees.CONFIG.commonConfigHolder.get();
	}
}
