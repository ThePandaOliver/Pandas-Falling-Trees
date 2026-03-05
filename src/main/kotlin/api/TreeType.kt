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
package dev.pandasystems.fallingtrees.api

import dev.pandasystems.universalserializer.elements.TreeElement
import dev.pandasystems.universalserializer.elements.TreeObject
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import kotlin.reflect.KProperty

abstract class TreeType {
    val enabled: Boolean = true

    abstract fun canPlayerFellTree(player: Player, level: Level, pos: BlockPos, state: BlockState = level.getBlockState(pos)): Boolean
    abstract fun scanForTree(level: Level, pos: BlockPos, state: BlockState = level.getBlockState(pos)): TreeBlob?
    abstract fun validateTree(blob: TreeBlob): Boolean
}