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
		priority = 100,
		detector = VerticalColumnDetector,
		enabledPredicate = { it.enableBamboo },
		trunkMatcher = BlockMatcher.ofBlocks(Blocks.BAMBOO),
		groundMatcher = DefaultGroundMatchers.BAMBOO_GROUND,
		minLeaves = 0,
		requireNaturalLeaves = false,
		allowDiagonalTrunks = false
	)

	val CACTUS: TreeDefinition = TreeDefinition(
		id = id(modId, "cactus"),
		priority = 100,
		detector = VerticalColumnDetector,
		enabledPredicate = { it.enableCactus },
		trunkMatcher = BlockMatcher.ofBlocks(Blocks.CACTUS),
		decorationMatcher = BlockMatcher.ofBlocks(Blocks.CACTUS_FLOWER),
		groundMatcher = DefaultGroundMatchers.CACTUS_GROUND,
		minLeaves = 0,
		requireNaturalLeaves = false,
		allowDiagonalTrunks = false
	)

	val GIANT_MUSHROOM: TreeDefinition = TreeDefinition(
		id = id(modId, "giant_mushroom"),
		priority = 90,
		detector = StandardTreeDetector,
		enabledPredicate = { it.enableGiantMushrooms },
		trunkMatcher = BlockMatcher.ofBlocks(Blocks.MUSHROOM_STEM),
		leafMatcher = BlockMatcher.ofBlocks(Blocks.RED_MUSHROOM_BLOCK, Blocks.BROWN_MUSHROOM_BLOCK),
		groundMatcher = DefaultGroundMatchers.STANDARD_GROUND,
		minLeaves = 3,
		requireNaturalLeaves = false
	)

	val GIANT_MUSHROOM_CAP_ONLY: TreeDefinition = TreeDefinition(
		id = id(modId, "giant_mushroom_cap"),
		priority = 85,
		detector = StandardTreeDetector,
		enabledPredicate = { it.enableGiantMushrooms },
		trunkMatcher = BlockMatcher.ofBlocks(
			Blocks.MUSHROOM_STEM,
			Blocks.RED_MUSHROOM_BLOCK,
			Blocks.BROWN_MUSHROOM_BLOCK
		),
		leafMatcher = BlockMatcher.NONE,
		groundMatcher = DefaultGroundMatchers.STANDARD_GROUND,
		minLeaves = 0,
		requireNaturalLeaves = false
	)

	val NETHER_FUNGUS: TreeDefinition = TreeDefinition(
		id = id(modId, "nether_fungus"),
		priority = 80,
		detector = StandardTreeDetector,
		enabledPredicate = { it.enableNetherFungi },
		trunkMatcher = BlockMatcher.ofBlocks(
			Blocks.CRIMSON_STEM,
			Blocks.STRIPPED_CRIMSON_STEM,
			Blocks.CRIMSON_HYPHAE,
			Blocks.STRIPPED_CRIMSON_HYPHAE,
			Blocks.WARPED_STEM,
			Blocks.STRIPPED_WARPED_STEM,
			Blocks.WARPED_HYPHAE,
			Blocks.STRIPPED_WARPED_HYPHAE
		),
		leafMatcher = BlockMatcher.ofTags(BlockTags.WART_BLOCKS),
		decorationMatcher = BlockMatcher.ofBlocks(Blocks.SHROOMLIGHT, Blocks.WEEPING_VINES, Blocks.WEEPING_VINES_PLANT),
		groundMatcher = DefaultGroundMatchers.STANDARD_GROUND,
		minLeaves = 3,
		requireNaturalLeaves = false
	)

	val CHORUS_PLANT: TreeDefinition = TreeDefinition(
		id = id(modId, "chorus_plant"),
		priority = 80,
		detector = StandardTreeDetector,
		enabledPredicate = { it.enableChorusPlants },
		trunkMatcher = BlockMatcher.ofBlocks(Blocks.CHORUS_PLANT),
		leafMatcher = BlockMatcher.ofBlocks(Blocks.CHORUS_FLOWER),
		groundMatcher = DefaultGroundMatchers.CHORUS_GROUND,
		minLeaves = 1,
		requireNaturalLeaves = false,
		allowDiagonalTrunks = false
	)

	val MANGROVE_TREE: TreeDefinition = TreeDefinition(
		id = id(modId, "mangrove_tree"),
		priority = 50,
		detector = StandardTreeDetector,
		trunkMatcher = BlockMatcher.ofBlocks(
			Blocks.MANGROVE_LOG,
			Blocks.STRIPPED_MANGROVE_LOG,
			Blocks.MANGROVE_WOOD,
			Blocks.STRIPPED_MANGROVE_WOOD,
			Blocks.MANGROVE_ROOTS
		),
		leafMatcher = BlockMatcher.ofBlocks(Blocks.MANGROVE_LEAVES) or BlockMatcher.ofTags(BlockTags.LEAVES),
		decorationMatcher = STANDARD_DECORATIONS or BlockMatcher.ofBlocks(Blocks.MANGROVE_PROPAGULE),
		groundMatcher = DefaultGroundMatchers.STANDARD_GROUND,
		requireNaturalLeaves = true
	)

	val STANDARD_TREE: TreeDefinition = TreeDefinition(
		id = id(modId, "standard_tree"),
		priority = 0,
		detector = StandardTreeDetector,
		trunkMatcher = BlockMatcher.ofTags(BlockTags.LOGS, BlockTags.OVERWORLD_NATURAL_LOGS),
		leafMatcher = BlockMatcher.ofTags(BlockTags.LEAVES, BlockTags.WART_BLOCKS),
		decorationMatcher = STANDARD_DECORATIONS,
		groundMatcher = DefaultGroundMatchers.STANDARD_GROUND,
		requireNaturalLeaves = true
	)

	fun register(definition: TreeDefinition): TreeDefinition {
		definitions.removeIf { it.id == definition.id }
		definitions.add(definition)
		definitions.sortByDescending { it.priority }
		return definition
	}

	fun allDefinitions(): List<TreeDefinition> = definitions

	fun registerDefaults() {
		register(BAMBOO)
		register(CACTUS)
		register(GIANT_MUSHROOM)
		register(GIANT_MUSHROOM_CAP_ONLY)
		register(NETHER_FUNGUS)
		register(CHORUS_PLANT)
		register(MANGROVE_TREE)
		register(STANDARD_TREE)
	}

	fun findMatchingDefinitions(state: BlockState, config: CommonConfig): List<TreeDefinition> {
		val blacklist = BlockMatcher.fromConfigStrings(config.blacklistedBlocks)
		if (blacklist.matches(state)) return emptyList()

		val extraLogs = BlockMatcher.fromConfigStrings(config.extraLogBlocks)
		return definitions.filter { def ->
			def.isEnabled(config) &&
				(def.trunkMatcher.matches(state) || (def.id == STANDARD_TREE.id && extraLogs.matches(state)))
		}
	}

	fun scanTree(level: BlockGetter, origin: BlockPos, config: CommonConfig): ScannedTree? {
		val originState = level.getBlockState(origin)
		val blacklist = BlockMatcher.fromConfigStrings(config.blacklistedBlocks)
		if (blacklist.matches(originState)) return null

		val candidates = findMatchingDefinitions(originState, config)
		for (definition in candidates) {
			val scanned = definition.detector.scan(level, origin, definition, config, blacklist)
			if (scanned != null && scanned.shouldFall(config)) {
				return scanned
			}
		}
		return null
	}
}
