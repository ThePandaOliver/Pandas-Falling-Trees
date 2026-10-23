package dev.pandasystems.fallingtrees.tree

import dev.pandasystems.pandalib.core.handles.Identifier
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState

fun interface BlockMatcher {
	fun matches(state: BlockState): Boolean

	infix fun or(other: BlockMatcher): BlockMatcher = BlockMatcher { state ->
		this.matches(state) || other.matches(state)
	}

	infix fun and(other: BlockMatcher): BlockMatcher = BlockMatcher { state ->
		this.matches(state) && other.matches(state)
	}

	companion object {
		val NONE = BlockMatcher { false }

		fun ofBlocks(vararg blocks: Block): BlockMatcher {
			val set = blocks.toSet()
			return BlockMatcher { it.block in set }
		}

		fun ofTags(vararg tags: TagKey<Block>): BlockMatcher {
			val list = tags.toList()
			return BlockMatcher { state -> list.any { state.`is`(it) } }
		}

		fun fromConfigStrings(entries: List<String>): BlockMatcher {
			if (entries.isEmpty()) return NONE
			val blockIds = mutableSetOf<Identifier>()
			val tagKeys = mutableListOf<TagKey<Block>>()

			for (raw in entries) {
				val trimmed = raw.trim()
				if (trimmed.isEmpty()) continue
				if (trimmed.startsWith("#")) {
					val id = Identifier.tryParse(trimmed.substring(1)) ?: continue
					tagKeys.add(TagKey.create(Registries.BLOCK, id))
				} else {
					val id = Identifier.tryParse(trimmed) ?: continue
					blockIds.add(id)
				}
			}

			return BlockMatcher { state ->
				val blockKey = BuiltInRegistries.BLOCK.getKey(state.block)
				blockKey in blockIds || tagKeys.any { state.`is`(it) }
			}
		}

		fun matchesItemConfigStrings(stack: ItemStack, entries: List<String>): Boolean {
			if (stack.isEmpty || entries.isEmpty()) return false
			for (raw in entries) {
				val trimmed = raw.trim()
				if (trimmed.isEmpty()) continue
				if (trimmed.startsWith("#")) {
					val id = Identifier.tryParse(trimmed.substring(1)) ?: continue
					val tag = TagKey.create(Registries.ITEM, id)
					if (stack.`is`(tag)) return true
				} else {
					val id = Identifier.tryParse(trimmed) ?: continue
					if (BuiltInRegistries.ITEM.getKey(stack.item) == id) return true
				}
			}
			return false
		}
	}
}
