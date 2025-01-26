import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.task.RemapJarTask

architectury {
	platformSetupLoomIde()
	fabric()
}

loom {
	accessWidenerPath.set(project(":common").loom.accessWidenerPath)
}

configurations {
	getByName("developmentFabric").extendsFrom(configurations["common"])
}

repositories {
	maven("https://maven.terraformersmc.com/releases/")
}

dependencies {
	modImplementation("net.fabricmc:fabric-loader:${properties["fabric_version"]}")
	modApi("net.fabricmc.fabric-api:fabric-api:${properties["fabric_api_version"]}")

	modImplementation("me.pandamods:pandalib-fabric:${properties["deps_pandalib_version"]}")
	modApi("dev.architectury:architectury-fabric:${properties["deps_architectury_version"]}")
	modApi("com.terraformersmc:modmenu:${properties["deps_modmenu_version"]}")

	modCompileOnly("maven.modrinth:treechop:${properties["deps_ht_treechop_version"]}-fabric,${properties["deps_ht_treechop_mc_version"]}")
//	modRuntimeOnly("maven.modrinth:treechop:${properties["deps_ht_treechop_version"]}-fabric,${properties["deps_ht_treechop_mc_version"]}")
//	modLocalRuntime("fuzs.forgeconfigapiport:forgeconfigapiport-fabric:${properties["deps_forge_config_api_version"]}")

	modCompileOnly("maven.modrinth:jade:${properties["deps_jade_version"]}+fabric-fabric,${properties["deps_jade_mc_version"]}")
//	modLocalRuntime("maven.modrinth:jade:${properties["deps_jade_version"]}+fabric-fabric,${properties["deps_jade_mc_version"]}")

	common(project(":common", "namedElements")) { isTransitive = false }
	shadowBundle(project(":common", "transformProductionFabric"))
}

tasks.remapJar {
	injectAccessWidener.set(true)
}

tasks.withType<RemapJarTask> {
	val shadowJar = tasks.getByName<ShadowJar>("shadowJar")
	inputFile.set(shadowJar.archiveFile)
}