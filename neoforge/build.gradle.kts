import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

architectury {
	platformSetupLoomIde()
	neoForge()
}

loom {
	accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

configurations {
	getByName("developmentNeoForge").extendsFrom(configurations["common"])
}

dependencies {
	neoForge("net.neoforged:neoforge:${properties["neoforge_version"]}")

	modImplementation("me.pandamods:pandalib-neoforge:${properties["deps_pandalib_version"]}")
	modApi("dev.architectury:architectury-neoforge:${properties["deps_architectury_version"]}")

	modCompileOnly("maven.modrinth:jade:${properties["deps_jade_version"]}+neoforge-neoforge,${properties["deps_jade_mc_version"]}")
//	modLocalRuntime("maven.modrinth:jade:${properties["deps_jade_version"]}+neoforge-neoforge,${properties["deps_jade_mc_version"]}")

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