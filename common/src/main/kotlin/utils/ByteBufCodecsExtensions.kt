/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU General Public License v3.0
 * See: https://www.gnu.org/licenses/gpl-3.0-standalone.html
 */
@file:JvmName("PLByteBufCodecs")

package dev.pandasystems.fallingtrees.utils

import io.netty.buffer.ByteBuf
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import java.util.UUID

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