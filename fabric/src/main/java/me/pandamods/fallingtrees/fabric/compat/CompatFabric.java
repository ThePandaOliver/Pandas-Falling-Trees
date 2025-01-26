/*
 * Copyright (C) 2024 Oliver Froberg (The Panda Oliver)
 *
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 * You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package me.pandamods.fallingtrees.fabric.compat;

import me.pandamods.fallingtrees.compat.Compat;
import me.pandamods.fallingtrees.compat.TreeChopCompat;
import org.jetbrains.annotations.Nullable;

public class CompatFabric implements Compat {
	private TreeChopCompat treeChopCompat;

	public CompatFabric() {
		if (Compat.hasTreeChop())
			treeChopCompat = new TreeChopCompatImpl();
	}

	@Override
	@Nullable
	public TreeChopCompat getTreeChopCompat() {
		return treeChopCompat;
	}
}
