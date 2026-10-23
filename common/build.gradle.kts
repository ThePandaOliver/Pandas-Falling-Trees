plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.fabric.loom)
    `maven-publish`
}

base {
    archivesName.set("fallingtrees-common")
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    compileOnly("net.fabricmc:fabric-loader:${project.property("fabric_loader_version")}")

    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines)
    api(libs.kotlinx.serialization)
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.serialization.cbor)
    api(libs.kotlinx.io)
    api(libs.kotlinx.io.bytestring)
    api(libs.kotlinx.datetime)

    testImplementation(libs.kotlin.test)

    api(libs.pandalib.common)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

kotlin {
    jvmToolchain(25)
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

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            artifactId = "fallingtrees-common"
        }
    }

    repositories {
        maven {
            name = "LocalRepo"
            url = uri(providers.gradleProperty("LocalRepo").getOrElse("${rootProject.layout.buildDirectory.get()}/repo"))
        }
    }
}
