package dev.johnoreilly.sqldelight

import app.cash.sqldelight.core.SqlDelightEnvironment
import app.cash.sqldelight.core.SqlDelightEnvironment.CompilationStatus
import app.cash.sqldelight.dialect.api.SqlDelightDialect
import app.cash.sqldelight.gradle.SqlDelightCompilationUnitImpl
import app.cash.sqldelight.gradle.SqlDelightDatabaseOptionsImpl
import app.cash.sqldelight.gradle.SqlDelightSourceFolderImpl
import org.jetbrains.amper.plugins.Input
import org.jetbrains.amper.plugins.Output
import org.jetbrains.amper.plugins.TaskAction
import java.nio.file.Path
import java.util.ServiceLoader
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

/**
 * Does what SQLDelight's Gradle `generate<Source>Interface` task does: compiles the `.sq` files in
 * [sourceDir] into Kotlin database/query classes in [outputDir].
 */
@OptIn(ExperimentalPathApi::class)
@TaskAction
fun generateSqlDelightInterface(
    @Input sourceDir: Path,
    @Output outputDir: Path,
    moduleName: String,
    settings: Settings,
) {
    outputDir.deleteRecursively()

    val environment = SqlDelightEnvironment(
        properties = SqlDelightDatabaseOptionsImpl(
            packageName = settings.packageName,
            className = settings.className,
            dependencies = emptyList(),
        ),
        compilationUnit = SqlDelightCompilationUnitImpl(
            name = "commonMain",
            sourceFolders = setOf(SqlDelightSourceFolderImpl(sourceDir.toFile())),
            outputDirectoryFile = outputDir.toFile(),
        ),
        verifyMigrations = false,
        moduleName = moduleName,
        // Load from the plugin's own classloader: the task runs in an isolated one.
        dialect = ServiceLoader.load(SqlDelightDialect::class.java, SqlDelightDialect::class.java.classLoader).first(),
    )

    when (val status = environment.generateSqlDelightFiles { println(it) }) {
        is CompilationStatus.Success -> Unit
        is CompilationStatus.Failure -> error("SQLDelight generation failed:\n" + status.errors.joinToString("\n"))
    }
}
