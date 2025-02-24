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

package me.pandamods.fallingtrees.registry;

import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import me.pandamods.fallingtrees.FallingTrees;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SoundRegistry {
	public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(FallingTrees.MOD_ID,
			Registries.SOUND_EVENT);

	public static final RegistrySupplier<SoundEvent> TREE_FALL = SOUNDS.register("tree_fall", () ->
			createFixedRangeEvent(FallingTrees.resourceLocation("tree_fall"), 16));

	public static final RegistrySupplier<SoundEvent> TREE_IMPACT = SOUNDS.register("tree_impact", () ->
			createFixedRangeEvent(FallingTrees.resourceLocation("tree_impact"), 16));

	private static SoundEvent createFixedRangeEvent(ResourceLocation resourceLocation, int range) {
		return SoundEvent.createFixedRangeEvent(resourceLocation, range);
	}
}
