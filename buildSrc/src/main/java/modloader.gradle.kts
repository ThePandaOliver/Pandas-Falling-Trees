/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU Lesser General Public License v3.0
 * See: https://www.gnu.org/licenses/lgpl-3.0-standalone.html
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.gradle.ext.packagePrefix
import org.jetbrains.gradle.ext.settings

plugins {
	id("common")
	id("com.gradleup.shadow")
}

val modId: String by extra
val modGroup: String by extra

loom {
	accessWidenerPath = project(":common").loom.accessWidenerPath

	runs {
		named("client") {
			client()
			configName = "Client"
			runDir("../.runs/client")
			programArg("--username=Dev")
			ideConfigGenerated(true)
		}
		named("server") {
			server()
			configName = "Server"
			runDir("../.runs/server")
			ideConfigGenerated(true)
		}
	}
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

tasks {
	getByName<ShadowJar>("shadowJar") {
		configurations = listOf(shadowBundle)
		archiveClassifier.set("dev-shadow")

		exclude("architectury.common.json")
	}

	remapJar {
		injectAccessWidener.set(true)
		inputFile = getByName<ShadowJar>("shadowJar").archiveFile
	}
}

idea {
	module {
		settings {
			val packagePrefixStr = "$modGroup.$modId.${project.name.lowercase()}"
			packagePrefix["src/main/kotlin"] = packagePrefixStr
			packagePrefix["src/main/java"] = packagePrefixStr
		}
	}
}