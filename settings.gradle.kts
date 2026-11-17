pluginManagement {
	repositories {
		mavenLocal()
		maven("https://maven.architectury.dev/") { name = "Architectury" }
		maven("https://maven.fabricmc.net/") { name = "Fabric" }
		maven("https://maven.neoforged.net/releases/") { name = "NeoForge" }
		gradlePluginPortal()
	}
}

plugins {
	// This plugin allows Gradle to automatically download arbitrary versions of Java for you
	id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "FallingTrees"

include("common")
include("fabric")
include("neoforge")
