/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU Lesser General Public License v3.0
 * See: https://www.gnu.org/licenses/lgpl-3.0-standalone.html
 */

plugins {
	`kotlin-dsl`
}

repositories {
	mavenCentral()
	gradlePluginPortal()
	maven("https://maven.architectury.dev/")
	maven("https://maven.parchmentmc.org/")
	maven("https://maven.fabricmc.net/")
	maven("https://maven.minecraftforge.net/")
	maven("https://maven.neoforged.net/releases/")
}

dependencies {
	fun applyPlugin(id: String, version: String) {
		implementation("$id:$id.gradle.plugin:$version")
	}

	applyPlugin("org.jetbrains.kotlin.jvm", "2.2.0")
	applyPlugin("org.jetbrains.gradle.plugin.idea-ext", "1.1.10")
	applyPlugin("architectury-plugin", "3.4-SNAPSHOT")
	applyPlugin("dev.architectury.loom", "1.11-SNAPSHOT")
	applyPlugin("com.gradleup.shadow", "9.0.2")
}