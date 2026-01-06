/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU Lesser General Public License v3.0
 * See: https://www.gnu.org/licenses/lgpl-3.0-standalone.html
 */

pluginManagement.repositories {
	mavenLocal()
	maven("https://maven.architectury.dev/") { name = "Architectury" }
	maven("https://maven.fabricmc.net/") { name = "Fabric" }
	maven("https://maven.neoforged.net/releases/") { name = "NeoForge" }
	maven("https://repo.pandasystems.dev/repository/maven-snapshots/") {
		name = "PandasRepository"
		mavenContent {
			snapshotsOnly()
		}
	}
	gradlePluginPortal()
}

include("fabric")
include("neoforge")

rootProject.name = "fallingtrees"