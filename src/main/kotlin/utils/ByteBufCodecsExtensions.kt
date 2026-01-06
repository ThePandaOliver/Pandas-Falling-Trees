/*
 * Copyright (C) 2026 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
@file:JvmName("PLByteBufCodecs")

package dev.pandasystems.fallingtrees.utils

import io.netty.buffer.ByteBuf
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import java.util.*

fun <T : Enum<T>> enumByteBufCodec(enumClass: Class<T>): StreamCodec<FriendlyByteBuf, T> {
	return StreamCodec.of(
		{ buf, value -> buf.writeEnum(value) },
		{ buf -> buf.readEnum(enumClass) }
	)
}

inline fun <reified T : Enum<T>> enumByteBufCodec(): StreamCodec<FriendlyByteBuf, T> {
	return enumByteBufCodec(T::class.java)
}

val uuidByteBufCodec: StreamCodec<ByteBuf, UUID> = StreamCodec.composite(
	ByteBufCodecs.STRING_UTF8, UUID::toString,
	UUID::fromString
)