pluginManagement.repositories {
	mavenLocal()
	maven(providers.gradleProperty("LocalRepo"))
	maven("https://maven.architectury.dev/") { name = "Architectury" }
	maven("https://maven.fabricmc.net/") { name = "Fabric" }
	maven("https://maven.neoforged.net/releases/") { name = "NeoForge" }
	gradlePluginPortal()
}

rootProject.name = "FallingTrees"

include("common")
include("fabric")
