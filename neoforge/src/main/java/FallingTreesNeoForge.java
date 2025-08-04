/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU General Public License v3.0
 * See: https://www.gnu.org/licenses/gpl-3.0-standalone.html
 */

package dev.pandasystems.fallingtrees.neoforge;

import dev.pandasystems.fallingtrees.FallingTrees;
import dev.pandasystems.fallingtrees.utils.BlockMapEntityData;
import dev.pandasystems.fallingtrees.utils.ItemListEntityData;
import dev.pandasystems.pandalib.api.registry.deferred.DeferredRegister;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

@Mod(FallingTrees.MOD_ID)
public class FallingTreesNeoForge {
	public final static DeferredRegister<EntityDataSerializer<?>> ENTITY_DATA =
			DeferredRegister.create(FallingTrees.MOD_ID, NeoForgeRegistries.ENTITY_DATA_SERIALIZERS);

    public FallingTreesNeoForge(IEventBus eventBus) {
		FallingTrees instance = FallingTrees.INSTANCE;

		ENTITY_DATA.register("block_map", key -> BlockMapEntityData.BLOCK_MAP);
		ENTITY_DATA.register("item_list", key -> ItemListEntityData.ITEM_LIST);
		ENTITY_DATA.register();
	}
}
