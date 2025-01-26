package me.pandamods.fallingtrees.compat;

import dev.architectury.platform.Platform;

public interface Compat {
	static boolean hasTreeChop() {
		return Platform.isModLoaded("treechop");
	}

	TreeChopCompat getTreeChopCompat();
}