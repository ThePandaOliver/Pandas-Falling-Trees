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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemListEntityData implements EntityDataSerializer<List<ItemStack>> {
	public static final EntityDataSerializer<List<ItemStack>> ITEM_LIST = new ItemListEntityData();

	@Override
	public void write(FriendlyByteBuf buffer, List<ItemStack> value) {
		buffer.writeCollection(value, FriendlyByteBuf::writeItem);
	}

	@Override
	public @NotNull List<ItemStack> read(FriendlyByteBuf buffer) {
		return buffer.readList(FriendlyByteBuf::readItem);
	}

	@Override
	public @NotNull List<ItemStack> copy(List<ItemStack> value) {
		return new ArrayList<>(value);
	}
}
