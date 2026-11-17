import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.tasks.Jar
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.maven
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
	id("org.jetbrains.kotlin.jvm")
	id("org.jetbrains.kotlin.plugin.serialization")
	`maven-publish`
}

repositories {
	maven {
		name = "PandaLibRepo"
		url = uri("file:///C:/Users/Olive/IdeaProjects/PandaLib/build/repo")
		content {
			includeGroup("dev.pandasystems")
		}
	}
	mavenLocal()
	mavenCentral()
}

configurations.configureEach {
	resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
	"api"(libs.findLibrary("kotlin-reflect").get())
	"api"(libs.findLibrary("kotlinx-coroutines").get())
	"api"(libs.findLibrary("kotlinx-serialization").get())
	"api"(libs.findLibrary("kotlinx-serialization-json").get())
	"api"(libs.findLibrary("kotlinx-serialization-cbor").get())
	"api"(libs.findLibrary("kotlinx-io").get())
	"api"(libs.findLibrary("kotlinx-io-bytestring").get())
	"api"(libs.findLibrary("kotlinx-datetime").get())

	"testImplementation"(libs.findLibrary("kotlin-test").get())
}

extensions.configure<JavaPluginExtension> {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(25))
	}
}

extensions.configure<KotlinJvmProjectExtension> {
	jvmToolchain(25)
}

configureRootBuildCopy(tasks.named<Jar>("jar"))
