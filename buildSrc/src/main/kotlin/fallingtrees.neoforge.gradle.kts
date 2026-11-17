import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.jvm.tasks.Jar

plugins {
	id("fallingtrees.kotlin")
	id("com.google.devtools.ksp")
	id("com.gradleup.shadow")
	id("net.neoforged.moddev")
}

val common = configurations.create("common") {
	isCanBeResolved = true
	isCanBeConsumed = false
}

val shadowCommon = configurations.create("shadowCommon") {
	isCanBeResolved = true
	isCanBeConsumed = false
}

neoForge {
	enable {
		version = project.property("neoforge_version") as String
	}
}

configurations.named("compileClasspath").configure {
	extendsFrom(common)
}
configurations.named("runtimeClasspath").configure {
	extendsFrom(common)
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
	"implementation"(libs.findLibrary("autoService-annotations").get())
	"ksp"(libs.findLibrary("autoService-ksp").get())
}

tasks.named<Jar>("jar") {
	archiveClassifier.set("dev")
}

val shadowJarTask = tasks.named<ShadowJar>("shadowJar") {
	configurations = listOf(shadowCommon)
	archiveClassifier.set("")
	mergeServiceFiles()
	from(tasks.named("jarJar"))
}

configurations.named("apiElements").configure {
	outgoing.artifacts.clear()
	outgoing.artifact(shadowJarTask)
}

configurations.named("runtimeElements").configure {
	outgoing.artifacts.clear()
	outgoing.artifact(shadowJarTask)
}

tasks.named("assemble") {
	dependsOn(shadowJarTask)
}

setPrimaryBuildArchive(shadowJarTask)
