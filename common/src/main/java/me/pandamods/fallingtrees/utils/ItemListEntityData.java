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

package me.pandamods.fallingtrees.utils;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ItemListEntityData {
		public static final EntityDataSerializer<List<ItemStack>> ITEM_LIST = new EntityDataSerializer<>() {
		@Override
		public StreamCodec<? super RegistryFriendlyByteBuf, List<ItemStack>> codec() {
			return ItemStack.OPTIONAL_LIST_STREAM_CODEC;
		}

		@Override
		public List<ItemStack> copy(List<ItemStack> value) {
			return value.stream().map(ItemStack::copy).toList();
		}
	};
}
