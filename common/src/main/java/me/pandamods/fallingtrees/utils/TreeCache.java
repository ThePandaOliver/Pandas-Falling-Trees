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

package me.pandamods.fallingtrees.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import me.pandamods.fallingtrees.api.TreeData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class TreeCache {
	public  static final Map<UUID, TreeSpeed> TREE_SPEED_CACHES = new ConcurrentHashMap<>();
	
	public record TreeSpeed(float miningSpeed, BlockPos blockPos) {}
}
