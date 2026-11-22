package dev.johnoreilly.peopleinspace

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.johnoreilly.peopleinspace.db.People
import dev.johnoreilly.peopleinspace.db.PeopleInSpaceDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

class PeopleInSpaceDatabaseTest {
    @Test
    fun insertsAndReadsPeople() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PeopleInSpaceDatabase.Schema.create(driver)
        val queries = PeopleInSpaceDatabase(driver).peopleInSpaceQueries

        queries.insertItem(People("Jane Doe", "ISS", null, null, "Irish"))
        queries.insertItem(People("Jane Doe", "Tiangong", null, null, "Irish")) // replaces on primary key

        assertEquals(listOf("Jane Doe" to "Tiangong"), queries.selectAll { name, craft, _, _, _ -> name to craft }.executeAsList())
    }
}
