pluginManagement.repositories {
	maven {
		name = "Architectury"
		url = uri("https://maven.architectury.dev/")
	}
	maven {
		name = "Fabric"
		url = uri("https://maven.fabricmc.net/")
	}
	maven {
		name = "Forge"
		url = uri("https://maven.minecraftforge.net/")
	}
	maven {
		name = "NeoForge"
		url = uri("https://maven.neoforged.net/releases/")
	}
	gradlePluginPortal()
}

include("common", "fabric", "neoforge")