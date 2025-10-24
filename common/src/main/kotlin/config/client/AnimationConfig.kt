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

package dev.pandasystems.fallingtrees.config.client

import dev.pandasystems.pandalib.config.options.configOption

class AnimationConfig {
	val fallAnimLength by configOption(2.5f)
	val bounceAngleHeight by configOption(10f)
	val bounceAnimLength by configOption(1f)
}
