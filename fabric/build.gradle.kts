plugins {
	id("fallingtrees.fabric")
}

evaluationDependsOn(":common")

base {
	archivesName.set("fallingtrees-fabric")
}

dependencies {
	"common"(project(":common")) { isTransitive = false }
	"shadowCommon"(project(":common")) { isTransitive = false }

	api(libs.pandalib.fabric)
}

loom {
	runs {
		named("client") {
			client()
			configName = "Fabric Client"
			ideConfigFolder = "Fabric"
			ideConfigGenerated(true)
			runDir("../.runs/client")
		}
		named("server") {
			server()
			configName = "Fabric Server"
			ideConfigFolder = "Fabric"
			ideConfigGenerated(true)
			runDir("../.runs/server")
		}
	}
}

tasks.processResources {
	from(project(":common").tasks.processResources)
	val replaceProperties = mapOf(
		"mod_id" to project.property("mod_id"),
		"mod_name" to project.property("mod_name"),
		"mod_version" to project.property("mod_version"),
		"mod_description" to project.property("mod_description"),
		"mod_authors" to project.property("mod_authors"),
		"mod_license" to project.property("mod_license"),
		"minecraft_version" to project.property("minecraft_version"),
		"fabric_loader_version" to project.property("fabric_loader_version"),
		"fabric_api_version" to project.property("fabric_api_version")
	)
	inputs.properties(replaceProperties)
	filesMatching("fabric.mod.json") {
		expand(replaceProperties)
	}
}

configureFallingTreesPublishing("fallingtrees-fabric")
