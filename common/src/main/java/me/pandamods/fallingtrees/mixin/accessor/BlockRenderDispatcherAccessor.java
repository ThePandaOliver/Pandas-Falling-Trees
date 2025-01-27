package me.pandamods.fallingtrees.mixin.accessor;

import net.minecraft.client.renderer.SpecialBlockModelRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Supplier;

@Mixin(BlockRenderDispatcher.class)
public interface BlockRenderDispatcherAccessor {
	@Accessor
	Supplier<SpecialBlockModelRenderer> getSpecialBlockModelRenderer();
}
