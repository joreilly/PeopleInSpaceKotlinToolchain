package dev.johnoreilly.peopleinspace

import kotlin.test.Test
import kotlin.test.assertEquals

class PeopleInSpaceApiTest {
    @Test
    fun parsesAstrosResponse() {
        val result = json.decodeFromString<AstroResult>(
            """{"message":"success","number":1,"people":[{"craft":"ISS","name":"Jane Doe","nationality":"Irish"}]}"""
        )
        assertEquals(listOf(Assignment(craft = "ISS", name = "Jane Doe", nationality = "Irish")), result.people)
    }

    @Test
    fun parsesIssPositionResponse() {
        val position = json.decodeFromString<IssPosition>(
            """{"name":"iss","id":25544,"latitude":51.5,"longitude":-9.05,"altitude":420.1}"""
        )
        assertEquals(IssPosition(51.5, -9.05), position)
    }
}
