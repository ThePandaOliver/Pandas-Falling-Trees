import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

fun Project.configureFallingTreesPublishing(artifactId: String) {
	extensions.configure<PublishingExtension> {
		publications {
			create<MavenPublication>("maven") {
				from(components["java"])
				this.artifactId = artifactId
			}
		}

		repositories {
			maven {
				name = "LocalRepo"
				url = uri(providers.gradleProperty("LocalRepo").getOrElse("${rootProject.layout.buildDirectory.get()}/repo"))
			}
		}
	}
}
