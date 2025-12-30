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

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.world.item.ItemStack

object ItemListEntityData : EntityDataSerializer<MutableList<ItemStack>> {
	override fun write(buffer: FriendlyByteBuf, value: MutableList<ItemStack>) {
		buffer.writeVarInt(value.size)
		value.forEach {
			buffer.writeItem(it)
		}
	}

	override fun read(buffer: FriendlyByteBuf): MutableList<ItemStack>? {
		val size = buffer.readVarInt()
		val list = mutableListOf<ItemStack>()
		repeat(size) {
			list.add(buffer.readItem())
		}
		return list
	}

	override fun copy(value: MutableList<ItemStack>): MutableList<ItemStack> {
		return value.toMutableList()
	}
}
