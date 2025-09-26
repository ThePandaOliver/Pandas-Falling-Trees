/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU Lesser General Public License v3.0
 * See: https://www.gnu.org/licenses/lgpl-3.0-standalone.html
 */
@file:Suppress("UnstableApiUsage")

import net.fabricmc.loom.task.RemapJarTask


plugins {
	id("architectury-plugin") apply false
	id("dev.architectury.loom") apply false
	id("com.gradleup.shadow") apply false
	id("io.github.pacifistmc.forgix") version "2.0.0-SNAPSHOT.5.1-FORK.3"
	
	id("me.modmuss50.mod-publish-plugin") version "0.8.4"
	id("com.google.devtools.ksp") version "2.2.0-2.0.2"
	`maven-publish`
}

val modVersion: String by project
val modGroup: String by project

version = modVersion
group = modGroup

subprojects {
	apply(plugin = "org.jetbrains.kotlin.jvm")
	apply(plugin = "org.jetbrains.gradle.plugin.idea-ext")
	apply(plugin = "com.google.devtools.ksp")
	apply(plugin = "maven-publish")

	repositories {
		maven {
			name = "Github"
			url = uri("https://maven.pkg.github.com/ThePandaOliver/Forgix")
			credentials {
				username = System.getenv("GITHUB_USER")
				password = System.getenv("GITHUB_API_TOKEN")
			}
		}
		mavenLocal()
	}

	dependencies {
		runtimeOnly("com.google.auto.service:auto-service-annotations:1.1.1")
		compileOnly("com.google.auto.service:auto-service-annotations:1.1.1")
		ksp("dev.zacsweers.autoservice:auto-service-ksp:1.2.0")
	}

	publishing {
		repositories {
			maven {
				name = "Github"
				url = uri("https://maven.pkg.github.com/ThePandaOliver/Pandas-Falling-Trees")
				credentials {
					username = System.getenv("GITHUB_USER")
					password = System.getenv("GITHUB_API_TOKEN")
				}
			}
		}
	}
}

evaluationDependsOnChildren()

forgix {
	archiveClassifier = ""

	findProject(":fabric")?.let {
		fabric {
			inputJar = it.tasks.named<RemapJarTask>("remapJar").get().archiveFile
		}
	}

	findProject(":neoforge")?.let {
		neoforge {
			inputJar = it.tasks.named<RemapJarTask>("remapJar").get().archiveFile
		}
	}

	findProject(":forge")?.let {
		forge {
			inputJar = it.tasks.named<RemapJarTask>("remapJar").get().archiveFile
		}
	}
}