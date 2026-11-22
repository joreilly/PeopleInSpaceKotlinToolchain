package dev.johnoreilly.peopleinspace

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import dev.johnoreilly.peopleinspace.db.People
import dev.johnoreilly.peopleinspace.db.PeopleInSpaceDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

/** People are cached in SQLDelight, so the last-known crew shows straight away (and offline). */
class PeopleInSpaceRepository(
    private val api: PeopleInSpaceApi,
    database: PeopleInSpaceDatabase,
) {
    private val queries = database.peopleInSpaceQueries

    val people: Flow<List<Assignment>> =
        queries.selectAll { name, craft, personImageUrl, personBio, nationality ->
            Assignment(craft, name, personImageUrl, personBio, nationality)
        }.asFlow().mapToList(Dispatchers.IO)

    /** Fetches the crew and replaces the cached list; returns what was fetched. */
    suspend fun refreshPeople(): List<Assignment> {
        val people = api.fetchPeople().people
        queries.transaction {
            queries.deleteAll()
            people.forEach {
                queries.insertItem(People(it.name, it.craft, it.personImageUrl, it.personBio, it.nationality))
            }
        }
        return people
    }

    suspend fun fetchIssPosition(): IssPosition = api.fetchIssPosition()
}
