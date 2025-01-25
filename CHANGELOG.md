# Version 0.13 Beta

## User Changelog
* Added error handling when destroying tree.

### Dependency Updates
* Updated pandalib 0.4.2 -> 0.5

### Config Update
* Renamed `standardTree` config to `genericTree`
* Renamed `extraBlockFilter` config to `adjacentBlockFilter`
* Changed `maxLeavesRadius` default value 10 -> 7
* Removed `shouldFallOnMaxLogAmount` as it became redundant with the new algorithm changes to Generic Tree
* Renamed `onlyFallWithRequiredTool` to `requireTool`
* Removed `mushroomTree` filters, this is now handled internally

### Algorithm Update
* Highly improved the Generic Tree's tree detection algorithm by using the Breadth-first search (BFS) method.
* Chorus Plants will now only fall from the mined block and above, leaving the blocks under the mined block.

## Developer Notes
* Changed `TreeRegistry` to use deferred registration
* Removed `TreeTypeRegistry.class` in favor of deferred registration
* Replaced `BlockStateMixin` with `BlockBehaviourMixin`
* Moved `MakeTreeFall` method from `EventHandler` to new `TreeHandler` class
* Renamed `standardTree.class` to `genericTree.class`
* Renamed `standardTreeConfig.class` to `genericTreeConfig.class`
* Renamed `Tree.class` to `TreeType.class`
* Split `MushroomTree.class` up into `RedMushroomTree.class` and `BrownMushroomTree.class`
