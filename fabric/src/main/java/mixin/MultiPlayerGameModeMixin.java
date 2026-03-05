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

package dev.pandasystems.fallingtrees.fabric.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
//	@Shadow private BlockPos destroyBlockPos;
//
//	@Shadow @Final private Minecraft minecraft;
//
//	@Inject(method = "startDestroyBlock", at = @At("RETURN"))
//	public void startDestroyBlock(BlockPos loc, Direction face, CallbackInfoReturnable<Boolean> cir) {
//		MultiPlayerGameModeKtImpl.INSTANCE.startDestroyBlock(face);
//	}
//
//	@Inject(method = "tick", at = @At("RETURN"))
//	public void tick(CallbackInfo ci) {
//		MultiPlayerGameModeKtImpl.INSTANCE.tick(minecraft, destroyBlockPos, (MultiPlayerGameMode) (Object) this);
//	}
}
