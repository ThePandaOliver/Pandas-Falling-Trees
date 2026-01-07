/*
 * Copyright (C) 2025 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.pandasystems.fallingtrees.utils

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.world.item.ItemStack

object ItemListEntityData : EntityDataSerializer<MutableList<ItemStack>> {
	override fun codec(): StreamCodec<in RegistryFriendlyByteBuf, MutableList<ItemStack>> {
		return ItemStack.OPTIONAL_LIST_STREAM_CODEC
	}

	override fun copy(value: MutableList<ItemStack>): MutableList<ItemStack> {
		return value.stream().map { obj: ItemStack? -> obj!!.copy() }.toList()
	}
}
