/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU Lesser General Public License v3.0
 * See: https://www.gnu.org/licenses/lgpl-3.0-standalone.html
 */

pluginManagement.repositories {
	maven("https://maven.architectury.dev/") { name = "Architectury" }
	maven("https://maven.fabricmc.net/") { name = "Fabric" }
	maven("https://maven.minecraftforge.net/") { name = "Forge" }
	maven("https://maven.neoforged.net/releases/") { name = "NeoForge" }
	maven("https://maven.pkg.github.com/ThePandaOliver/Forgix") {
		name = "Github Forgix"
		credentials {
			username = System.getenv("GITHUB_USER")
			password = System.getenv("GITHUB_API_TOKEN")
		}
	}
	gradlePluginPortal()
}

include("fabric")

rootProject.name = "fallingtrees"