/*
 * Copyright (C) 2025-2025 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.pandasystems.fallingtrees.fabric.mixin;

import dev.pandasystems.fallingtrees.FallingTreesRegistriesKt;
import dev.pandasystems.fallingtrees.config.FallingTreesConfigKt;
import dev.pandasystems.fallingtrees.mixin.MultiPlayerGameModeKtImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
	@Shadow private BlockPos destroyBlockPos;

	@Shadow @Final private Minecraft minecraft;

	@Unique
	private MultiPlayerGameModeKtImpl fallingtrees$impl = new MultiPlayerGameModeKtImpl();

	@Inject(method = "startDestroyBlock", at = @At("RETURN"))
	public void startDestroyBlock(BlockPos loc, Direction face, CallbackInfoReturnable<Boolean> cir) {
		fallingtrees$impl.startDestroyBlock(face);
	}

	@Inject(method = "tick", at = @At("RETURN"))
	public void tick(CallbackInfo ci) {
		fallingtrees$impl.tick(minecraft, destroyBlockPos, (MultiPlayerGameMode) (Object) this);
	}
}
