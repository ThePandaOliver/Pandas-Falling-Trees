package me.pandamods.fallingtrees.forge;

import dev.architectury.platform.forge.EventBuses;
import dev.architectury.utils.Env;
import me.pandamods.fallingtrees.FallingTrees;
import me.pandamods.fallingtrees.forge.client.FallingTreesClientForge;
import me.pandamods.fallingtrees.forge.compat.CompatForge;
import me.pandamods.pandalib.utils.EnvRunner;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FallingTrees.MOD_ID)
public class FallingTreesForge {
    public FallingTreesForge() {
		IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        EventBuses.registerModEventBus(FallingTrees.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        FallingTrees.init();
		CompatForge.init();

		EnvRunner.runIf(Env.CLIENT, () -> () -> new FallingTreesClientForge(eventBus));
    }
}
