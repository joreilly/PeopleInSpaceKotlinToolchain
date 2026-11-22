package dev.johnoreilly.peopleinspace

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.johnoreilly.peopleinspace.db.PeopleInSpaceDatabase
import kotlin.test.Test
import kotlin.test.assertEquals

class PeopleInSpaceDatabaseTest {
    @Test
    fun insertsAndReadsPeople() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PeopleInSpaceDatabase.Schema.create(driver)
        val queries = PeopleInSpaceDatabase(driver).peopleInSpaceQueries

        queries.insertItem("Jane Doe", "ISS")
        queries.insertItem("Jane Doe", "Tiangong") // replaces on primary key

        assertEquals(listOf("Jane Doe" to "Tiangong"), queries.selectAll { name, craft -> name to craft }.executeAsList())
    }
}
