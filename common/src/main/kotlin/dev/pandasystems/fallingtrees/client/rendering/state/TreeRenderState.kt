package dev.pandasystems.fallingtrees.client.rendering.state

import net.minecraft.client.renderer.block.MovingBlockRenderState
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3

class TreeRenderState : EntityRenderState() {
	class SingleBlockState(
		val relativePos: BlockPos,
		val movingBlockRenderState: MovingBlockRenderState
	)

	var blockRenderStates: List<SingleBlockState> = emptyList()
	var fallDirection: Vec3 = Vec3(1.0, 0.0, 0.0)
	var fallProgress: Float = 0f
}
