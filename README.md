![Panda's Falling Tree's Banner](assets/banner.png)

> [![Discord](https://img.shields.io/discord/1021703635178115122?style=for-the-badge&logo=discord&label=Discord&labelColor=black&color=lightblue)](https://discord.gg/wjPt4vEfXb)
> [![Modrinth](https://img.shields.io/modrinth/dt/i2kUe4lq?style=for-the-badge&logo=modrinth&label=Modrinth&labelColor=black&color=green)](https://modrinth.com/mod/pandas-falling-trees)
> [![Curseforge](https://img.shields.io/curseforge/dt/880630?style=for-the-badge&logo=curseforge&label=Curseforge&labelColor=black&color=red)](https://www.curseforge.com/minecraft/mc-mods/pandas-falling-trees)
> [![GitHub](https://img.shields.io/github/downloads/PandaDap2006/Pandas-Falling-Trees/total?style=for-the-badge&logo=github&label=Github&labelColor=black&color=white)](https://github.com/PandaDap2006/Pandas-Falling-Trees)
>
> [![PandaLib](https://img.shields.io/badge/PandaLib-REQUIRED-1?style=for-the-badge&labelColor=black&color=gold)](https://www.curseforge.com/minecraft/mc-mods/pandalib)
> [![Fabric API](https://img.shields.io/badge/Fabric%20API-REQUIRED%20for%20Fabric-1?style=for-the-badge&labelColor=black&color=gold)](https://www.curseforge.com/minecraft/mc-mods/fabric-api)

## About:

Panda's Falling Trees makes every tree fall like trees from [Dynamic trees](https://www.curseforge.com/minecraft/mc-mods/dynamictrees), while still maintaining
the original trees from Vanilla Minecraft.

The mod should support other mods if log and leaves blocks are a part of the log and leaves Block Tag

### Supported versions and mod loaders:

| Mod loader | Versions          |
|------------|-------------------|
| Fabric     | 1.20 – 1.21.10    |
| NeoForge   | 1.20.5 – 1.21.10  |
| Forge      | Support has ended |

Development is targeted 1.21.10

## Showcase:

![Tree falling showcase](assets/showcase_falling_tree_large.gif)

---

### Development:

#### Looking for a specific version's codebase

- **1.21**
  - [1.21.10](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.21.10)
  - [1.21.9](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.21.9)
  - [1.21.8](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.21.8)
  - [1.21.7](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.21.7)
  - [1.21.6](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.21.6)
  - [1.21.5](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.21.5)
  - [1.21.4](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.21.4)
  - [1.21.3](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.21.3)
  - [1.21.2](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.21.2)
  - [1.21.1](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.21.1)
  - [1.21.0](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.21)
- **1.20**
  - [1.20.6](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.20.6)
  - [1.20.5](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.20.5)
  - [1.20.4](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.20.4)
  - [1.20.3](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.20.3)
  - [1.20.2](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.20.2)
  - [1.20.1](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.20.1)
  - [1.20.0](https://github.com/ThePandaOliver/Pandas-Falling-Trees/tree/versions/1.20)

#### Kotlin DSL

```kotlin
repositories {
	mavenCentral()
	maven("https://repo.pandasystems.dev/maven/maven/")
}

dependencies {
	modApi("dev.pandasystems:fallingtrees-common-<game version>:<version>") // Common
	api("dev.pandasystems:fallingtrees-neoforge-<game version>:<version>")  // NeoForge
	modApi("dev.pandasystems:fallingtrees-fabric-<game version>:<version>") // Fabric
}
```

---

## Advertisement:

> ### Thanks to **Kinetic Hosting** for supporting this project
> [![Partner Banner](https://github.com/ThePandaOliver/ThePandaOliver/blob/main/assets_for_readme/Support/kinetic_hosting_banner.png?raw=true)](https://billing.kinetichosting.com/aff.php?aff=476)
>
> Every purchased server via my [affiliate link](https://billing.kinetichosting.com/aff.php?aff=476) will help support me and my work.

## License

The project is licensed under the GNU GPLv3