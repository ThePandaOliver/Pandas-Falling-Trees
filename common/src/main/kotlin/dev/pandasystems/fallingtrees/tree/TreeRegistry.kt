package dev.pandasystems.fallingtrees.tree

import dev.pandasystems.fallingtrees.config.CommonConfig
import dev.pandasystems.fallingtrees.modId
import dev.pandasystems.pandalib.core.handles.id
import net.minecraft.core.BlockPos
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import java.util.concurrent.CopyOnWriteArrayList

object TreeRegistry {
	private val definitions = CopyOnWriteArrayList<TreeDefinition>()

	val STANDARD_DECORATIONS: BlockMatcher = BlockMatcher.ofBlocks(
		Blocks.VINE,
		Blocks.COCOA,
		Blocks.BEE_NEST,
		Blocks.SHROOMLIGHT,
		Blocks.MOSS_CARPET,
		Blocks.PALE_MOSS_CARPET,
		Blocks.PALE_HANGING_MOSS,
		Blocks.CREAKING_HEART
	)

	val BAMBOO: TreeDefinition = TreeDefinition(
		id = id(modId, "bamboo"),
		detector = TreeDetectors.COLUMN_ID,
		logs = BlockMatcher.ofBlocks(Blocks.BAMBOO)
	)

	val CACTUS: TreeDefinition = TreeDefinition(
		id = id(modId, "cactus"),
		detector = TreeDetectors.COLUMN_ID,
		logs = BlockMatcher.ofBlocks(Blocks.CACTUS),
		decorations = BlockMatcher.ofBlocks(Blocks.CACTUS_FLOWER)
	)

	val GIANT_MUSHROOM: TreeDefinition = TreeDefinition(
		id = id(modId, "giant_mushroom"),
		logs = BlockMatcher.ofBlocks(
			Blocks.MUSHROOM_STEM,
			Blocks.RED_MUSHROOM_BLOCK,
			Blocks.BROWN_MUSHROOM_BLOCK
		)
	)

	val NETHER_FUNGUS: TreeDefinition = TreeDefinition(
		id = id(modId, "nether_fungus"),
		logs = BlockMatcher.ofBlocks(
			Blocks.CRIMSON_STEM,
			Blocks.STRIPPED_CRIMSON_STEM,
			Blocks.CRIMSON_HYPHAE,
			Blocks.STRIPPED_CRIMSON_HYPHAE,
			Blocks.WARPED_STEM,
			Blocks.STRIPPED_WARPED_STEM,
			Blocks.WARPED_HYPHAE,
			Blocks.STRIPPED_WARPED_HYPHAE
		),
		leaves = BlockMatcher.ofTags(BlockTags.WART_BLOCKS),
		decorations = BlockMatcher.ofBlocks(
			Blocks.SHROOMLIGHT,
			Blocks.WEEPING_VINES,
			Blocks.WEEPING_VINES_PLANT
		)
	)

	val CHORUS_PLANT: TreeDefinition = TreeDefinition(
		id = id(modId, "chorus_plant"),
		detector = TreeDetectors.CARDINAL_BRANCHING_ID,
		logs = BlockMatcher.ofBlocks(Blocks.CHORUS_PLANT),
		leaves = BlockMatcher.ofBlocks(Blocks.CHORUS_FLOWER)
	)

	val MANGROVE_TREE: TreeDefinition = TreeDefinition(
		id = id(modId, "mangrove_tree"),
		logs = BlockMatcher.ofBlocks(
			Blocks.MANGROVE_LOG,
			Blocks.STRIPPED_MANGROVE_LOG,
			Blocks.MANGROVE_WOOD,
			Blocks.STRIPPED_MANGROVE_WOOD,
			Blocks.MANGROVE_ROOTS
		),
		leaves = BlockMatcher.ofBlocks(Blocks.MANGROVE_LEAVES) or BlockMatcher.ofTags(BlockTags.LEAVES),
		decorations = STANDARD_DECORATIONS or BlockMatcher.ofBlocks(Blocks.MANGROVE_PROPAGULE)
	)

	val STANDARD_TREE: TreeDefinition = TreeDefinition(
		id = id(modId, "standard_tree"),
		logs = BlockMatcher.ofTags(BlockTags.LOGS, BlockTags.OVERWORLD_NATURAL_LOGS),
		leaves = BlockMatcher.ofTags(BlockTags.LEAVES, BlockTags.WART_BLOCKS),
		decorations = STANDARD_DECORATIONS
	)

	fun register(definition: TreeDefinition): TreeDefinition {
		definitions.removeIf { it.id == definition.id }
		definitions.add(definition)
		return definition
	}

	fun allDefinitions(): List<TreeDefinition> = definitions

	fun registerDefaults() {
		register(BAMBOO)
		register(CACTUS)
		register(GIANT_MUSHROOM)
		register(NETHER_FUNGUS)
		register(CHORUS_PLANT)
		register(MANGROVE_TREE)
		register(STANDARD_TREE)
	}

	fun isDefinitionEnabled(definition: TreeDefinition, config: CommonConfig): Boolean =
		when (definition.id) {
			BAMBOO.id -> config.enableBamboo
			CACTUS.id -> config.enableCactus
			GIANT_MUSHROOM.id -> config.enableGiantMushrooms
			NETHER_FUNGUS.id -> config.enableNetherFungi
			CHORUS_PLANT.id -> config.enableChorusPlants
			else -> true
		}

	fun findMatchingDefinitions(state: BlockState, config: CommonConfig): List<TreeDefinition> {
		val blacklist = BlockMatcher.fromConfigStrings(config.blacklistedBlocks)
		if (blacklist.matches(state)) return emptyList()

		val extraLogs = BlockMatcher.fromConfigStrings(config.extraLogBlocks)
		return definitions.filter { def ->
			isDefinitionEnabled(def, config) &&
				(def.logs.matches(state) || (def.id == STANDARD_TREE.id && extraLogs.matches(state)))
		}
	}

	fun scanTree(level: BlockGetter, origin: BlockPos, config: CommonConfig): ScannedTree? {
		val originState = level.getBlockState(origin)
		val blacklist = BlockMatcher.fromConfigStrings(config.blacklistedBlocks)
		if (blacklist.matches(originState)) return null

		val extraLogs = BlockMatcher.fromConfigStrings(config.extraLogBlocks)
		val extraLeaves = BlockMatcher.fromConfigStrings(config.extraLeafBlocks)

		val candidates = findMatchingDefinitions(originState, config)
		for (definition in candidates) {
			val effectiveLogs = if (definition.id == STANDARD_TREE.id) {
				definition.logs or extraLogs
			} else {
				definition.logs
			}
			val effectiveLeaves = if (definition.hasLeaves) {
				definition.leaves or extraLeaves
			} else {
				BlockMatcher.NONE
			}

			val context = TreeScanContext(
				level = level,
				origin = origin,
				definition = definition,
				config = config,
				blacklist = blacklist,
				effectiveLogs = effectiveLogs,
				effectiveLeaves = effectiveLeaves
			)

			val detector = TreeDetectors.get(definition.detector)
			val scanned = detector.scan(context)
			if (scanned != null && scanned.shouldFall(config)) {
				return scanned
			}
		}
		return null
	}
}
