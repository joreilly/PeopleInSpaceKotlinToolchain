package dev.johnoreilly.sqldelight

import org.jetbrains.amper.plugins.Configurable

@Configurable
interface Settings {
    /** Package of the generated database class. */
    val packageName: String

    /** Name of the generated database class. */
    val className: String get() = "Database"
}
