plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ksp)
    alias(libs.plugins.easymodding)
    alias(libs.plugins.kotlin.serialization)
    `maven-publish`
}

base {
    archivesName.set("pandas-falling-trees")
}

repositories {
    mavenLocal()
    maven(providers.gradleProperty("LocalRepo"))
    mavenCentral()
}

dependencies {
    testImplementation(libs.kotlin.test)
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

kotlin {
    jvmToolchain(21)
}

easyModding {
    configPath = rootProject.file("easymodding.mod.json")
    minecraftVersion = "26.2"

    modDependencies {
        modImplementation("dev.pandasystems:pandalib-common:1.0.0-SNAPSHOT")
    }
}

publishing {
    repositories {
        maven {
            name = "LocalRepo"
            url = uri(providers.gradleProperty("LocalRepo"))
        }
    }
}