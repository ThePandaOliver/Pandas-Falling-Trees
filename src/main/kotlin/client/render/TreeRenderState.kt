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

package dev.pandasystems.fallingtrees.client.render

import dev.pandasystems.fallingtrees.api.TreeType
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState

class TreeRenderState : EntityRenderState() {
	var treeType: TreeType? = null
	var blocks: MutableMap<BlockPos, BlockState>? = null
	var lifeTime: Double = 0.0
	var direction: Direction? = null
	var level: Level? = null
	var originPos: Vec3i? = null
}
