package dev.pandasystems.fallingtrees.client.rendering

import dev.pandasystems.fallingtrees.client.rendering.state.TreeRenderState
import dev.pandasystems.fallingtrees.core.FallingTreesRegistries
import dev.pandasystems.fallingtrees.core.entity.TreeEntity
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider

class TreeRenderer(context: EntityRendererProvider.Context) : EntityRenderer<TreeEntity, TreeRenderState>(context) {
	override fun createRenderState(): TreeRenderState {
		TODO("Not yet implemented")
	}
}