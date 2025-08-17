/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU Lesser General Public License v3.0
 * See: https://www.gnu.org/licenses/lgpl-3.0-standalone.html
 */

@file:Suppress("UnstableApiUsage")

architectury {
	neoForge()
}

configurations {
	getByName("developmentNeoForge").extendsFrom(common.get())
}

val nonModImplementation: Configuration by configurations.creating
configurations.implementation.get().extendsFrom(nonModImplementation)

dependencies {
	neoForge(libs.neoforgeLoader)

	modApi(libs.pandalib.neoforge)

	forgeRuntimeLibrary(kotlin("stdlib"))
	forgeRuntimeLibrary(kotlin("stdlib-jdk8"))
	forgeRuntimeLibrary(kotlin("stdlib-jdk7"))
	forgeRuntimeLibrary(kotlin("reflect", version = "2.2.0"))
	forgeRuntimeLibrary("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
	forgeRuntimeLibrary("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.10.2")
	forgeRuntimeLibrary("org.jetbrains.kotlinx:kotlinx-serialization-core:1.8.1")
	forgeRuntimeLibrary("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
	forgeRuntimeLibrary("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.8.1")
	forgeRuntimeLibrary("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
	forgeRuntimeLibrary("org.jetbrains.kotlinx:kotlinx-io-core:0.7.0")
	forgeRuntimeLibrary("org.jetbrains.kotlinx:kotlinx-io-bytestring:0.7.0")

	common(project(":", configuration = "namedElements")) { isTransitive = false }
	shadowBundle(project(":", configuration = "transformProductionNeoForge"))
}

tasks.remapJar {
	atAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
}