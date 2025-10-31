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

import com.google.common.collect.Maps
import dev.pandasystems.pandalib.utils.codecs.StreamCodec
import io.netty.buffer.ByteBuf
import net.minecraft.core.BlockPos
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.VarInt
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

object BlockMapEntityData : EntityDataSerializer<MutableMap<BlockPos, BlockState>>  {
	var BLOCK_MAP_CODEC = object : StreamCodec<ByteBuf, MutableMap<BlockPos, BlockState>> {
		override fun decode(byteBuf: ByteBuf): MutableMap<BlockPos, BlockState> {
			val size = VarInt.read(byteBuf)
			val map: MutableMap<BlockPos, BlockState> = Maps.newHashMapWithExpectedSize(size)
			for (i in 0..<size) {
				map[BlockPos.of(byteBuf.readLong())] = Block.stateById(VarInt.read(byteBuf))
			}
			return map
		}

		override fun encode(byteBuf: ByteBuf, map: MutableMap<BlockPos, BlockState>) {
			VarInt.write(byteBuf, map.size)
			map.forEach { (blockPos: BlockPos, blockState: BlockState) ->
				byteBuf.writeLong(blockPos.asLong())
				VarInt.write(byteBuf, Block.getId(blockState))
			}
		}
	}

	@JvmField
	val BLOCK_MAP: EntityDataSerializer<MutableMap<BlockPos, BlockState>> = BlockMapEntityData

	override fun write(
		buffer: FriendlyByteBuf,
		value: MutableMap<BlockPos, BlockState>
	) {
		BLOCK_MAP_CODEC.encode(buffer, value)
	}

	override fun read(buffer: FriendlyByteBuf): MutableMap<BlockPos, BlockState>? {
		return BLOCK_MAP_CODEC.decode(buffer)
	}

	override fun copy(value: MutableMap<BlockPos, BlockState>): MutableMap<BlockPos, BlockState> {
		return value.toMutableMap()
	}
}
