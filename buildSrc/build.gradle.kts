plugins {
	`kotlin-dsl`
}

dependencies {
	implementation(pluginDep(libs.plugins.kotlin.jvm))
	implementation(pluginDep(libs.plugins.kotlin.serialization))
	implementation(pluginDep(libs.plugins.ksp))
	implementation(pluginDep(libs.plugins.shadow))
	implementation(pluginDep(libs.plugins.fabric.loom))
	implementation(pluginDep(libs.plugins.neoforge.moddev))
}

fun pluginDep(plugin: Provider<PluginDependency>): Provider<String> =
	plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
