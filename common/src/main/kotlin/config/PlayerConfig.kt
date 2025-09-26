/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU General Public License v3.0
 * See: https://www.gnu.org/licenses/gpl-3.0-standalone.html
 */

package dev.pandasystems.fallingtrees.config

import dev.pandasystems.fallingtrees.FallingTrees
import dev.pandasystems.fallingtrees.utils.enumByteBufCodec
import dev.pandasystems.fallingtrees.utils.uuidByteBufCodec
import dev.pandasystems.pandalib.event.serverevents.serverPlayerJoinEvent
import dev.pandasystems.pandalib.networking.PacketRegistry
import dev.pandasystems.pandalib.platform.game
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.PacketFlow
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import java.util.*

data class PlayerConfigPayload(
	val playerUuid: UUID,
	val miningShould: MiningOptionEnum = MiningOptionEnum.CHOP_TREE,
	val miningWhileCrouchingShould: MiningOptionEnum = MiningOptionEnum.MINE_SINGLE_BLOCK
) : CustomPacketPayload {
	override fun type(): CustomPacketPayload.Type<out CustomPacketPayload> = playerConfigPayloadType
}

val playerConfigPayloadType = CustomPacketPayload.Type<PlayerConfigPayload>(FallingTrees.resourceLocation("player_config"))

val playerConfigPayloadCodecs: StreamCodec<FriendlyByteBuf, PlayerConfigPayload> = StreamCodec.composite(
	uuidByteBufCodec, PlayerConfigPayload::playerUuid,
	enumByteBufCodec<MiningOptionEnum>(), PlayerConfigPayload::miningShould,
	enumByteBufCodec<MiningOptionEnum>(), PlayerConfigPayload::miningWhileCrouchingShould,
	::PlayerConfigPayload
)

internal val playerConfigs = mutableMapOf<UUID, PlayerConfigPayload>()

internal fun onPlayerConfigPacketReceived(packet: PlayerConfigPayload) {
	playerConfigs[packet.playerUuid] = packet
}

internal fun registerPlayerConfigPayload() {
	PacketRegistry.registerCodec(playerConfigPayloadType, playerConfigPayloadCodecs)
	PacketRegistry.registerHandler(PacketFlow.CLIENTBOUND, playerConfigPayloadType, ::onPlayerConfigPacketReceived)

	if (game.isClient) {

	}
}