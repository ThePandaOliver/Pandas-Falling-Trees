# Version 0.12.8 Beta

## User Changelog
### Dependency Updates
* Updated pandalib 0.4.2 -> 0.5

### Config Updates
* Renamed `standardTree` config to `genericTree`
* Renamed `extraBlockFilter` config to `adjacentBlockFilter`
* Changed `maxLeavesRadius` default value 10 -> 7
* Removed `shouldFallOnMaxLogAmount` as it became redundant with the new algorithm changes to Generic Tree


## Developer Notes
* Changed `TreeRegistry` to use deferred registration
* Removed `TreeTypeRegistry.class` in favor of deferred registration
* Replaced `BlockStateMixin` with `BlockBehaviourMixin`
* Moved `MakeTreeFall` method from `EventHandler` to new `TreeHandler` class
* Renamed `standardTree.class` to `genericTree.class`
* Renamed `standardTreeConfig.class` to `genericTreeConfig.class`
* Renamed `Tree.class` to `TreeType.class`
