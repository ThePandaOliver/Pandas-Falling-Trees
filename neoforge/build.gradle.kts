plugins {
	id("fallingtrees.neoforge")
}

evaluationDependsOn(":common")

base {
	archivesName.set("fallingtrees-neoforge")
}

val commonMainSourceSet = project(":common").extensions.getByType<SourceSetContainer>()["main"]

dependencies {
	"common"(project(":common")) { isTransitive = false }
	"shadowCommon"(project(":common")) { isTransitive = false }

	api(libs.pandalib.neoforge)
}

neoForge {
	runs {
		create("client") {
			client()
			ideName = "NeoForge Client"
			ideFolderName = "NeoForge"
			gameDirectory.set(rootProject.file(".runs/client"))
		}
		create("server") {
			server()
			ideName = "NeoForge Server"
			ideFolderName = "NeoForge"
			gameDirectory.set(rootProject.file(".runs/server"))
		}
	}

	mods {
		create(project.property("mod_id") as String) {
			sourceSet(sourceSets.main.get())
			sourceSet(commonMainSourceSet)
		}
	}
}

tasks.processResources {
	val replaceProperties = mapOf(
		"mod_id" to project.property("mod_id"),
		"mod_name" to project.property("mod_name"),
		"mod_version" to project.property("mod_version"),
		"mod_description" to project.property("mod_description"),
		"mod_authors" to project.property("mod_authors"),
		"mod_license" to project.property("mod_license"),
		"minecraft_version" to project.property("minecraft_version"),
		"neoforge_version" to project.property("neoforge_version")
	)
	inputs.properties(replaceProperties)
	filesMatching(listOf("META-INF/neoforge.mods.toml", "pack.mcmeta")) {
		expand(replaceProperties)
	}
}

configureFallingTreesPublishing("fallingtrees-neoforge")
