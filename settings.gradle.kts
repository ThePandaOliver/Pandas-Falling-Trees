/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU Lesser General Public License v3.0
 * See: https://www.gnu.org/licenses/lgpl-3.0-standalone.html
 */

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
	maven {
		name = "Github"
		url = uri("https://maven.pkg.github.com/ThePandaOliver/Forgix")
		credentials {
			username = System.getenv("GITHUB_USER")
			password = System.getenv("GITHUB_API_TOKEN")
		}
	}
	gradlePluginPortal()
}

include("common")
include("fabric")
include("neoforge")

rootProject.name = "fallingtrees"