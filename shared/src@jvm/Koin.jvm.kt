package dev.johnoreilly.peopleinspace

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.johnoreilly.peopleinspace.db.PeopleInSpaceDatabase
import org.koin.dsl.module
import java.io.File
import java.util.Properties

actual val platformModule = module {
    single<SqlDriver> {
        val dbFile = File(System.getProperty("user.home"), ".peopleinspace/$DATABASE_NAME").apply { parentFile.mkdirs() }
        JdbcSqliteDriver("jdbc:sqlite:${dbFile.path}", Properties(), PeopleInSpaceDatabase.Schema)
    }
}
