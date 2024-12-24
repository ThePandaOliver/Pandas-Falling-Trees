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

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class CollectionsUtils {
	public static <K, V> Map<K, V> mapRemoveIf(Map<K, V> map, BiFunction<K, V, Boolean> predicate) {
		Map<K, V> returnMap = new HashMap<>(map);
		map.forEach((k, v) -> {
			if (predicate.apply(k, v))
				returnMap.remove(k);
		});
		return returnMap;
	}
}
