package dev.johnoreilly.peopleinspace

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import dev.johnoreilly.peopleinspace.db.PeopleInSpaceDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<SqlDriver> { AndroidSqliteDriver(PeopleInSpaceDatabase.Schema, androidContext(), DATABASE_NAME) }
}
