import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

val isSnapshot = !hasProperty("snapshot") || findProperty("snapshot") == "true"

architectury {
	platformSetupLoomIde()
	neoForge()
}

loom {
	accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

configurations {
	getByName("developmentNeoForge").extendsFrom(configurations["common"])
	forgeRuntimeLibrary.get().extendsFrom(configurations["forgeJarShadow"])
}

dependencies {
	neoForge("net.neoforged:neoforge:${properties["neoforge_version"]}")

	modCompileOnly("dev.pandasystems:pandalib-neoforge:${properties["deps_pandalib_version"]}")
	runtimeOnly("dev.pandasystems:pandalib-neoforge:${properties["deps_pandalib_version"]}") {
		exclude(group = "net.neoforged.fancymodloader", module = "loader")
	}
	modApi("dev.architectury:architectury-neoforge:${properties["deps_architectury_version"]}")

	modCompileOnly("maven.modrinth:jade:${properties["deps_jade_neoforge_version"]}+neoforge-neoforge,${properties["deps_jade_mc_version"]}")
//	modLocalRuntime("maven.modrinth:jade:${properties["deps_jade_neoforge_version"]}+neoforge-neoforge,${properties["deps_jade_mc_version"]}")

	common(project(":common", "namedElements")) { isTransitive = false }
	shadowBundle(project(":common", "transformProductionNeoForge"))
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
}