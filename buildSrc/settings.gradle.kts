dependencyResolutionManagement {
	repositories {
		mavenLocal()
		maven("https://maven.architectury.dev/") { name = "Architectury" }
		maven("https://maven.fabricmc.net/") { name = "Fabric" }
		maven("https://maven.neoforged.net/releases/") { name = "NeoForge" }
		gradlePluginPortal()
		mavenCentral()
	}

	versionCatalogs {
		create("libs") {
			from(files("../gradle/libs.versions.toml"))
		}
	}
}
