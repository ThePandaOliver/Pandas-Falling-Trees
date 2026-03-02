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
package dev.pandasystems.fallingtrees.api

import dev.pandasystems.universalserializer.elements.TreeElement
import dev.pandasystems.universalserializer.elements.TreeObject
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import kotlin.reflect.KProperty

abstract class TreeType {
	private val _configValues = mutableListOf<ConfigValue<*>>()
	val configValues: List<ConfigValue<*>> get() = _configValues

	val enabled: Boolean by ConfigValue("enabled", true)

	abstract fun scanBlocks(level: Level, pos: BlockPos, state: BlockState = level.getBlockState(pos)) : TreeBlob
	abstract fun validateTree(blob: TreeBlob) : Boolean

	internal fun loadData(data: TreeObject) {
		val version = data["version"]?.asPrimitive?.asNumber?.toInt() ?: throw IllegalArgumentException("Tree config is missing version")
		if (version != 1) throw IllegalArgumentException("Tree config version [${version}] is not supported")

		_configValues.forEach { configValue ->
			var configElement: TreeElement = data
			configValue.name.split('.').forEach { name ->
				configElement = configElement.asObject[name] ?: throw IllegalArgumentException("The config value is missing [${configValue.name}]")
			}
			@Suppress("UNCHECKED_CAST")
			(configValue as ConfigValue<Any?>).value = TODO("Deserialize the config value")
		}
	}

	inner class ConfigValue<T>(val name: String, val defaultValue: T) {
		var value: T = defaultValue

		init {
			_configValues.add(this)
		}

		operator fun getValue(thisRef: TreeType, property: KProperty<*>): T {
			return value
		}

		operator fun setValue(thisRef: TreeType, property: KProperty<*>, value: T) {
			this.value = value
		}
	}
}