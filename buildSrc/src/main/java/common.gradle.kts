/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU Lesser General Public License v3.0
 * See: https://www.gnu.org/licenses/lgpl-3.0-standalone.html
 */

import org.jetbrains.gradle.ext.packagePrefix
import org.jetbrains.gradle.ext.settings

plugins {
	kotlin("jvm")

	id("architectury-plugin")
	id("dev.architectury.loom")

	id("org.jetbrains.gradle.plugin.idea-ext")
}

val javaVersion: String by extra
val mcVersion: String by extra

val modVersion: String by extra
val modId: String by extra
val modGroup: String by extra

val modName: String by extra
val modDescription: String by extra
val modAuthors: String by extra
val modLicense: String by extra

val fabricLoaderVersion: String? by extra
val neoforgeLoaderVersion: String? by extra

val parchmentMinecraftVersion: String by extra
val parchmentMappingsVersion: String by extra

architectury {
	minecraft = mcVersion
}

group = modGroup
version = modVersion
base { archivesName = "${rootProject.name}-${project.name}" }

loom {
	silentMojangMappingsLicense()
	log4jConfigs.from(rootProject.file("log4j2.xml"))
	accessWidenerPath = file("src/main/resources/$modId.accesswidener")

	decompilers {
		get("vineflower").apply { // Adds names to lambdas - useful for mixins
			options.put("mark-corresponding-synthetics", "1")
		}
	}
}

repositories {
	mavenCentral()
	maven("https://maven.architectury.dev/")
	maven("https://maven.parchmentmc.org/")
	maven("https://maven.fabricmc.net/")
	maven("https://maven.minecraftforge.net/")
	maven("https://maven.neoforged.net/releases/")
}

dependencies {
	minecraft("com.mojang:minecraft:$mcVersion")
	@Suppress("UnstableApiUsage")
	mappings(loom.layered {
		officialMojangMappings()
		parchment("org.parchmentmc.data:parchment-$parchmentMinecraftVersion:$parchmentMappingsVersion@zip")
	})
}

kotlin {
	jvmToolchain(21)
}

tasks {
	processResources {
		val props = mutableMapOf(
			"java_version" to javaVersion,
			"minecraft_version" to mcVersion,

			"mod_version" to modVersion,
			"mod_group" to modGroup,
			"mod_id" to modId,

			"mod_name" to modName,
			"mod_description" to modDescription,
			"mod_license" to modLicense,
			"mod_authors_fabric" to modAuthors.split(",").joinToString(", ") { "\"$it\"" },
			"mod_authors_forge" to modAuthors,
		)

		fabricLoaderVersion?.let { props["fabric_loader_version"] = it }
		neoforgeLoaderVersion?.let { props["neoforge_loader_version"] = it }

		inputs.properties(props)
		filesMatching(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "fabric.mod.json", "**.mixins.json", "pack.mcmeta")) {
			expand(props)
		}
	}
}

java {
	withSourcesJar()
}

idea {
	module {
		settings {
			val packagePrefixStr = "$modGroup.$modId"
			packagePrefix["src/main/kotlin"] = packagePrefixStr
			packagePrefix["src/main/java"] = packagePrefixStr
		}
	}
}