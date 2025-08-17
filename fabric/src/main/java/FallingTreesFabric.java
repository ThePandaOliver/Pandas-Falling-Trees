/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU General Public License v3.0
 * See: https://www.gnu.org/licenses/gpl-3.0-standalone.html
 */

package dev.pandasystems.fallingtrees.fabric;

import dev.pandasystems.fallingtrees.FallingTrees;
import dev.pandasystems.fallingtrees.utils.BlockMapEntityData;
import dev.pandasystems.fallingtrees.utils.ItemListEntityData;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricTrackedDataRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.network.syncher.EntityDataSerializers;

public class FallingTreesFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        FallingTrees instance = FallingTrees.INSTANCE;
    }
}
