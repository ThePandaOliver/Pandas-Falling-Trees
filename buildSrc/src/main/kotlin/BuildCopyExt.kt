import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.register
import java.nio.file.Files
import java.nio.file.StandardCopyOption

abstract class CopyBuildArtifactTask : DefaultTask() {
	@get:InputFile
	@get:PathSensitive(PathSensitivity.NAME_ONLY)
	abstract val sourceFile: RegularFileProperty

	@get:OutputFile
	abstract val destinationFile: RegularFileProperty

	@TaskAction
	fun copyArtifact() {
		val src = sourceFile.get().asFile.toPath()
		val dest = destinationFile.get().asFile.toPath()
		Files.createDirectories(dest.parent)
		Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING)
	}
}

internal class BuildCopyState(
	val sourceArchive: RegularFileProperty,
	var primaryArchiveTask: TaskProvider<out AbstractArchiveTask>,
	val copyTask: TaskProvider<CopyBuildArtifactTask>
)

private const val BUILD_COPY_STATE_KEY = "fallingTreesBuildCopyState"

private fun Project.normalizedBuildFolderName(): String =
	name.removeSuffix("-common")
		.removeSuffix("-fabric")
		.removeSuffix("-neoforge")

internal fun Project.configureRootBuildCopy(defaultArchiveTask: TaskProvider<out AbstractArchiveTask>) {
	val sourceArchive = objects.fileProperty().apply {
		set(defaultArchiveTask.flatMap { it.archiveFile })
	}

	val targetDir = rootProject.layout.buildDirectory.dir("libs/${normalizedBuildFolderName()}")
	val copyTask = tasks.register<CopyBuildArtifactTask>("copyBuildToRoot") {
		group = "build"
		description = "Copies this project's primary build artifact to the root build/libs directory."
		sourceFile.set(sourceArchive)
		destinationFile.set(
			targetDir.zip(sourceArchive) { dir, file ->
				dir.file(file.asFile.name)
			}
		)
	}

	extensions.extraProperties[BUILD_COPY_STATE_KEY] = BuildCopyState(
		sourceArchive = sourceArchive,
		primaryArchiveTask = defaultArchiveTask,
		copyTask = copyTask
	)

	tasks.named("assemble") {
		dependsOn(copyTask)
	}

	afterEvaluate {
		val state = extensions.extraProperties[BUILD_COPY_STATE_KEY] as BuildCopyState
		state.primaryArchiveTask.configure {
			finalizedBy(state.copyTask)
		}
	}
}

internal fun Project.setPrimaryBuildArchive(archiveTask: TaskProvider<out AbstractArchiveTask>) {
	val state = extensions.extraProperties[BUILD_COPY_STATE_KEY] as BuildCopyState
	state.primaryArchiveTask = archiveTask
	state.sourceArchive.set(archiveTask.flatMap { it.archiveFile })
}
