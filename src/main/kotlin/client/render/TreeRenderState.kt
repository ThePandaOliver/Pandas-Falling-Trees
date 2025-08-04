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
