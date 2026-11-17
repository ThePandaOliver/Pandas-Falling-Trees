plugins {
	id("fallingtrees.common")
}

base {
	archivesName.set("fallingtrees-common")
}

dependencies {
	api(libs.pandalib.common)
}

tasks.processResources {
	exclude("**/*.aup3") // Exclude all audacity files

	val replaceProperties = mapOf(
		"mod_id" to project.property("mod_id"),
		"mod_name" to project.property("mod_name"),
		"mod_version" to project.property("mod_version"),
		"mod_description" to project.property("mod_description"),
		"mod_authors" to project.property("mod_authors"),
		"mod_license" to project.property("mod_license"),
		"minecraft_version" to project.property("minecraft_version")
	)
	inputs.properties(replaceProperties)
	filesMatching("fallingtrees.mixins.json") {
		expand(replaceProperties)
	}
}

configureFallingTreesPublishing("fallingtrees-common")
