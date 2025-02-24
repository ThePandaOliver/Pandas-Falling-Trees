import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

val isSnapshot = !hasProperty("snapshot") || findProperty("snapshot") == "true"

architectury {
	platformSetupLoomIde()
	forge()
}

loom {
	accessWidenerPath.set(project(":common").loom.accessWidenerPath)

	forge {
		convertAccessWideners.set(true)
		extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)

		mixinConfig("${properties["mod_id"]}-common.mixins.json")
		mixinConfig("${properties["mod_id"]}.mixins.json")
	}
}

configurations {
	getByName("developmentForge").extendsFrom(configurations["common"])
	// Required for embedding libraries into the jar because Forge is weird.
	getByName("forgeRuntimeLibrary").extendsFrom(configurations["jarShadow"])
}

dependencies {
	forge("net.minecraftforge:forge:${properties["forge_version"]}")

	modImplementation("me.pandamods:pandalib-forge:${properties["deps_pandalib_version"]}")
	modApi("dev.architectury:architectury-forge:${properties["deps_architectury_version"]}")

	modCompileOnly("maven.modrinth:treechop:${properties["deps_ht_treechop_version"]}-forge,${properties["deps_ht_treechop_mc_version"]}")
//	modRuntimeOnly("maven.modrinth:treechop:${properties["deps_ht_treechop_version"]}-forge,${properties["deps_ht_treechop_mc_version"]}")

	modCompileOnly("maven.modrinth:jade:${properties["deps_jade_version"]}-forge,${properties["deps_jade_mc_version"]}")
//	modLocalRuntime("maven.modrinth:jade:${properties["deps_jade_version"]}-forge,${properties["deps_jade_mc_version"]}")


	common(project(":common", "namedElements")) { isTransitive = false }
	shadowBundle(project(":common", "transformProductionForge"))
}

tasks.remapJar {
	injectAccessWidener = true
	atAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
}

tasks.withType<RemapJarTask> {
	val shadowJar = tasks.getByName<ShadowJar>("shadowJar")
	inputFile.set(shadowJar.archiveFile)
}

publishing {
	publications {
		register("mavenJava", MavenPublication::class) {
			groupId = properties["maven_group"] as String
			artifactId = "${properties["mod_id"]}-${project.name}"
			version = "${project.version}"

			from(components["java"])
		}
	}

	repositories {
		maven {
			name = "Nexus"
			url = if (isSnapshot)
				uri("https://nexus.pandasystems.dev/repository/maven-snapshots/")
			else
				uri("https://nexus.pandasystems.dev/repository/maven-releases/")
			credentials {
				username = System.getenv("NEXUS_USERNAME")
				password = System.getenv("NEXUS_PASSWORD")
			}
		}
	}
}