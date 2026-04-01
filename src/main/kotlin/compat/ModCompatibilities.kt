package dev.pandasystems.fallingtrees.compat

import dev.pandasystems.pandalib.utils.modLoader

object ModCompatibilities {
    val isTreeChopLoaded get() = modLoader.isModLoaded("treechop")
}