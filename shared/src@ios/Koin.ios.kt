package dev.johnoreilly.peopleinspace

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import dev.johnoreilly.peopleinspace.db.PeopleInSpaceDatabase
import org.koin.dsl.module

actual val platformModule = module {
    single<SqlDriver> { NativeSqliteDriver(PeopleInSpaceDatabase.Schema, DATABASE_NAME) }
}
