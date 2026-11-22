package dev.johnoreilly.peopleinspace

import app.cash.sqldelight.db.SqlDriver
import dev.johnoreilly.peopleinspace.db.PeopleInSpaceDatabase
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(commonModule, platformModule)
}

val commonModule = module {
    single { PeopleInSpaceApi() }
    single { PeopleInSpaceDatabase(get<SqlDriver>()) }
    single { PeopleInSpaceRepository(get(), get()) }
    viewModelOf(::PersonListViewModel)
    viewModelOf(::ISSPositionViewModel)
}

/** Provides the platform's [SqlDriver]. */
expect val platformModule: Module

const val DATABASE_NAME = "peopleinspace.db"
