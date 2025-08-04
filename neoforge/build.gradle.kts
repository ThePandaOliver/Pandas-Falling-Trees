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

dependencies {
	neoForge(libs.neoforgeLoader)

	modApi(libs.pandalib.neoforge)
	runtimeOnly(libs.pandalib.neoforge) {
		exclude(group = "net.neoforged.fancymodloader", module = "loader")
	}

	common(project(":", configuration = "namedElements")) { isTransitive = false }
	shadowBundle(project(":", configuration = "transformProductionNeoForge"))
}

tasks.remapJar {
	atAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
}