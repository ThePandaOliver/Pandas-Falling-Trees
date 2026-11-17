plugins {
	id("fallingtrees.kotlin")
	id("net.fabricmc.fabric-loom")
}

dependencies {
	"minecraft"("com.mojang:minecraft:${project.property("minecraft_version")}")
	"compileOnly"("net.fabricmc:fabric-loader:${project.property("fabric_loader_version")}")
}
