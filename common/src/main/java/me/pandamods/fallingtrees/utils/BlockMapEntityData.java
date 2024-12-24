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

import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class BlockMapEntityData {
	public static StreamCodec<ByteBuf, Map<BlockPos, BlockState>> BLOCK_MAP_CODEC = new StreamCodec<>() {
		public @NotNull Map<BlockPos, BlockState> decode(ByteBuf byteBuf) {
			int size = VarInt.read(byteBuf);
			Map<BlockPos, BlockState> map = Maps.newHashMapWithExpectedSize(size);
			for (int i = 0; i < size; i++) {
				map.put(FriendlyByteBuf.readBlockPos(byteBuf), Block.stateById(VarInt.read(byteBuf)));
			}
			return map;
		}

		public void encode(ByteBuf byteBuf, Map<BlockPos, BlockState> map) {
			VarInt.write(byteBuf, map.size());
			map.forEach((blockPos, blockState) -> {
				FriendlyByteBuf.writeBlockPos(byteBuf, blockPos);
				VarInt.write(byteBuf, Block.getId(blockState));
			});
		}
	};

	public static final EntityDataSerializer<Map<BlockPos, BlockState>> BLOCK_MAP = EntityDataSerializer.forValueType(BLOCK_MAP_CODEC);
}
