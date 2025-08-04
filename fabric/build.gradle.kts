/*
 * Copyright (c) 2025. Oliver Froberg
 *
 * This code is licensed under the GNU Lesser General Public License v3.0
 * See: https://www.gnu.org/licenses/lgpl-3.0-standalone.html
 */

@file:Suppress("UnstableApiUsage")

architectury {
	fabric()
}

configurations {
	getByName("developmentFabric").extendsFrom(common.get())
}

repositories {
	maven("https://maven.terraformersmc.com/releases/")
}

dependencies {
	modImplementation(libs.fabricLoader)
	modApi(libs.fabricApi)
	modApi(libs.modmenu)

	modApi(libs.pandalib.fabric)

	common(project(":", configuration = "namedElements")) { isTransitive = false }
	shadowBundle(project(":", configuration = "transformProductionFabric"))
}