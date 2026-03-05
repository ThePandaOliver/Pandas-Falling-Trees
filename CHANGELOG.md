# Version 0.14.0 | Alpha 3

* Renamed GenericTree to OverworldTree
* Improved block destruction handling (TBT)
* Tree configs are now stored in separate files (WIP – Requires work on the config system in PandaLib)

## Reworked Mod API
* Fully reworked the mods API (WIP)
* Added multiple listenable events (TBD)
* Reimplemented all tree types to fit with the new API (WIP – Only OverworldTree has been implemented as of now)
* Improved caching of the tree data (WIP)

---

# Version 0.14.0 | Alpha 2

- Fixed an issue where it was depending on the wrong version of PandaLib.
- Cleaned up the codebase
- Updated to Fabric API 0.138.4
- Updated to Fabric Loader 0.18.4
- Updated to NeoForge 21.10.64
- Updated PandaLib to 1.0.0 Alpha 2

---

# Version 0.14.0 | Alpha 1

- Updated PandaLib to 1.0.0 Alpha 1

## Bug Fixes

- Fixed crash on server startup due to config options not being properly synchronized between server and client.\
  [#107](https://github.com/ThePandaOliver/Pandas-Falling-Trees/issues/107)
  [#121](https://github.com/ThePandaOliver/Pandas-Falling-Trees/issues/121)

---

# Version 0.13.3

- Updated PandaLib support to 0.5.3 to fix NeoForge incompatibility with the latest version
