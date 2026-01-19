/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU Lesser General Public License v3.0
 * See: https://www.gnu.org/licenses/lgpl-3.0-standalone.html
 */
@file:Suppress("UnstableApiUsage")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import me.modmuss50.mpp.ReleaseType
import net.fabricmc.loom.task.RemapJarTask
import org.jetbrains.gradle.ext.packagePrefix
import org.jetbrains.gradle.ext.settings

plugins {
	kotlin("jvm") version "2.2.0"
	id("architectury-plugin") version "3.4-SNAPSHOT"
	id("dev.architectury.loom") version "1.13-SNAPSHOT"
	id("com.gradleup.shadow") version "9.0.2" apply false
	id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.10"

	id("me.modmuss50.mod-publish-plugin") version "1.1.0"
	id("com.google.devtools.ksp") version "2.2.0-2.0.2"
	`maven-publish`
}

val javaVersion: String by project
val mcVersion: String by project
val buildFor: String by project

val modVersion: String by project
val modGroup: String by project
val modId: String by project

val modName: String by project
val modDescription: String by project
val modAuthors: String by project
val modLicense: String by project

val fabricLoaderVersion: String? by project
val neoforgeLoaderVersion: String? by project

val parchmentMinecraftVersion: String by project
val parchmentMappingsVersion: String by project

val fabricApiVersion: String by project
val pandalibVersion: String by project

allprojects {
	val loomPlatform = project.findProperty("loom.platform") as? String
	val loaderEnv = loomPlatform ?: "common"

	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "architectury-plugin")
	apply(plugin = "dev.architectury.loom")
	apply(plugin = "org.jetbrains.gradle.plugin.idea-ext")
	if (loomPlatform != null)
		apply(plugin = "com.gradleup.shadow")
	apply(plugin = "com.google.devtools.ksp")
	apply(plugin = "maven-publish")

	version = modVersion
	group = modGroup
	base { archivesName = "$modId-$loaderEnv-$mcVersion" }

	architectury {
		when (loomPlatform) {
			"fabric" -> {
				fabric()
				platformSetupLoomIde()
			}

			"neoforge" -> {
				neoForge()
				platformSetupLoomIde()
			}

			else -> {
				common(buildFor.split(",").map { it.trim() })
			}
		}
	}

	loom {
		silentMojangMappingsLicense()
		log4jConfigs.from(rootProject.file("log4j2.xml"))
		accessWidenerPath = rootProject.file("src/main/resources/$modId.accesswidener")

		decompilers {
			get("vineflower").apply { // Adds names to lambdas - useful for mixins
				options.put("mark-corresponding-synthetics", "1")
			}
		}

		if (loomPlatform != null) {
			runs {
				val path = project.projectDir.toPath().relativize(rootProject.file(".runs").toPath())

				named("client") {
					client()
					configName = "Client"
					runDir("$path/client")
					programArg("--username=Dev")
				}
				named("server") {
					server()
					configName = "Server"
					runDir("$path/server")
				}
			}
		}

		runs.configureEach { ideConfigGenerated(false) }
	}

	val common: Configuration by configurations.creating {
		isCanBeResolved = true
		isCanBeConsumed = false
	}
	configurations.compileClasspath.get().extendsFrom(common)
	configurations.runtimeClasspath.get().extendsFrom(common)

	val shadowBundle: Configuration by configurations.creating {
		isCanBeResolved = true
		isCanBeConsumed = false
	}

	configurations {
		when (loomPlatform) {
			"fabric" -> {
				getByName("developmentFabric").extendsFrom(common)
			}

			"neoforge" -> {
				getByName("developmentNeoForge").extendsFrom(common)
			}
		}
	}

	repositories {
		mavenLocal()
		mavenCentral()
		maven("https://maven.architectury.dev/")
		maven("https://maven.parchmentmc.org/")
		maven("https://maven.fabricmc.net/")
		maven("https://maven.neoforged.net/releases/")

		maven("https://repo.pandasystems.dev/repository/maven-public/")
	}

	dependencies {
		fun addNonMod(dependencyNotation: Any) {
			implementation(dependencyNotation)
			if (loomPlatform == "neoforge") {
				"forgeRuntimeLibrary"(dependencyNotation)
			}
		}
		addNonMod(kotlin("stdlib"))
		addNonMod(kotlin("stdlib-jdk8"))
		addNonMod(kotlin("stdlib-jdk7"))
		addNonMod(kotlin("reflect", version = "2.2.0"))
		addNonMod("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
		addNonMod("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.10.2")
		addNonMod("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.1")
		addNonMod("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
		addNonMod("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.8.1")
		addNonMod("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
		addNonMod("org.jetbrains.kotlinx:kotlinx-io-core:0.7.0")
		addNonMod("org.jetbrains.kotlinx:kotlinx-io-bytestring:0.7.0")

		addNonMod("dev.pandasystems:universal-serializer:0.1.0-SNAPSHOT")

		runtimeOnly("com.google.auto.service:auto-service-annotations:1.1.1")
		compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
		ksp("dev.zacsweers.autoservice:auto-service-ksp:1.2.0")

		minecraft("com.mojang:minecraft:$mcVersion")
		@Suppress("UnstableApiUsage")
		mappings(loom.layered {
			officialMojangMappings()
			parchment("org.parchmentmc.data:parchment-$parchmentMinecraftVersion:$parchmentMappingsVersion@zip")
		})

		when (loomPlatform) {
			"fabric" -> {
				modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
				modImplementation("net.fabricmc.fabric-api:fabric-api:$fabricApiVersion")

				common(project(":", configuration = "namedElements")) { isTransitive = false }
				shadowBundle(project(":", configuration = "transformProductionFabric"))
			}

			"neoforge" -> {
				"neoForge"("net.neoforged:neoforge:$neoforgeLoaderVersion")

				common(project(":", configuration = "namedElements")) { isTransitive = false }
				shadowBundle(project(":", configuration = "transformProductionNeoForge"))
			}

			else -> {
				// We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
				// Do NOT use other classes from fabric loader
				modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
			}
		}

		modImplementation("dev.pandasystems:pandalib-$loaderEnv-$mcVersion:$pandalibVersion:slim") { isTransitive = false }
	}

	java {
		withSourcesJar()
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

				"pandalib_version" to pandalibVersion
			)

			fabricLoaderVersion?.let { props["fabric_loader_version"] = it }
			neoforgeLoaderVersion?.let { props["neoforge_loader_version"] = it }

			inputs.properties(props)
			filesMatching(listOf("META-INF/mods.toml", "META-INF/neoforge.mods.toml", "fabric.mod.json", "**.mixins.json", "pack.mcmeta")) {
				expand(props)
			}
		}

		if (loomPlatform != null) {
			getByName<ShadowJar>("shadowJar") {
				configurations = listOf(shadowBundle)
				archiveClassifier.set("dev-shadow")

				exclude("architectury.common.json")
			}

			remapJar {
				injectAccessWidener.set(true)
				inputFile = getByName<ShadowJar>("shadowJar").archiveFile
				if (loomPlatform == "neoforge")
			 		atAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
			}

			val copyBuildModFile by registering(Copy::class) {
				from("build/libs/${base.archivesName.get()}-$version.jar")
				into(rootDir.resolve("build/mod-build"))
			}

			build {
				finalizedBy(copyBuildModFile)
			}

			val copyLicense by register("copyLicense",Copy::class) {
				from(rootDir.resolve("LICENSE.md"))
				destinationDir = project.layout.buildDirectory.file("resources/main").get().asFile
			}

			processResources {
				dependsOn(copyLicense)
			}
		}
	}

	idea {
		module {
			settings {
				val packagePrefixStr = "$modGroup.$modId".let {
					if (loomPlatform != null) "$it.$loomPlatform"
					else it
				}
				packagePrefix["src/main/kotlin"] = packagePrefixStr
				packagePrefix["src/main/java"] = packagePrefixStr
			}
		}
	}

	publishing {
		publications {
			create<MavenPublication>("maven") {
				from(components["java"])
				artifactId = base.archivesName.get()
			}
		}

		repositories {
			maven(
				if (version.toString().endsWith("-SNAPSHOT"))
					"https://repo.pandasystems.dev/repository/maven-snapshots/"
				else
					"https://repo.pandasystems.dev/repository/maven-releases/"
			) {
				name = "Nexus"
				credentials {
					username = System.getenv("NEXUS_USERNAME")
					password = System.getenv("NEXUS_PASSWORD")
				}
			}
		}
	}
}

publishMods {
	type = ReleaseType.ALPHA
	changelog = file("CHANGELOG.md").readText()

	val cfOptions = curseforgeOptions {
		accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
		projectId = "880630"
		minecraftVersions.add(mcVersion)
	}

	val mrOptions = modrinthOptions {
		accessToken = providers.environmentVariable("MODRINTH_TOKEN")
		projectId = "i2kUe4lq"
		minecraftVersions.add(mcVersion)
	}

	curseforge("curseforgeFabric") {
		displayName = "[$mcVersion Fabric] ${project.version}"
		from(cfOptions)
		file = project(":fabric").tasks.remapJar.get().archiveFile
		modLoaders.add("fabric")
		requires(
			"fabric-api",
			"pandalib"
		)
	}

	curseforge("curseforgeNeoForge") {
		displayName = "[$mcVersion NeoForge] ${project.version}"
		from(cfOptions)
		file = project(":neoforge").tasks.remapJar.get().archiveFile
		modLoaders.add("neoforge")
		requires("pandalib")
	}

	modrinth("modrinthFabric") {
		displayName = "[$mcVersion Fabric] ${project.version}"
		from(mrOptions)
		file = project(":fabric").tasks.remapJar.get().archiveFile
		modLoaders.add("fabric")
		requires(
			"fabric-api",
			"pandalib"
		)
	}

	modrinth("modrinthNeoForge") {
		displayName = "[$mcVersion NeoForge] ${project.version}"
		from(mrOptions)
		file = project(":neoforge").tasks.remapJar.get().archiveFile
		modLoaders.add("neoforge")
		requires("pandalib")
	}
}