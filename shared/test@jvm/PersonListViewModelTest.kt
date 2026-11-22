package dev.johnoreilly.peopleinspace

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import dev.johnoreilly.peopleinspace.db.PeopleInSpaceDatabase
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class PersonListViewModelTest {
    /** Each fetch of astros.json returns the next response; a null entry fails with HTTP 500. */
    private var responses = ArrayDeque<String?>()

    private val repository by lazy {
        val client = HttpClient(MockEngine {
            val body = responses.removeFirst()
            if (body == null) respond("boom", HttpStatusCode.InternalServerError)
            else respond(body, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType, "application/json"))
        }) {
            expectSuccess = true
            install(ContentNegotiation) { json(json) }
        }
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PeopleInSpaceDatabase.Schema.create(driver)
        PeopleInSpaceRepository(PeopleInSpaceApi(client), PeopleInSpaceDatabase(driver))
    }

    @BeforeTest
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun emptyCrewIsSuccessNotLoading() = runTest {
        responses += """{"message":"success","number":0,"people":[]}"""
        val viewModel = PersonListViewModel(repository)

        assertEquals(PersonListUiState.Success(emptyList()), viewModel.uiState.first { it !is PersonListUiState.Loading })
    }

    @Test
    fun failedFirstLoadCanBeRetried() = runTest {
        responses += null
        responses += ASTROS
        val viewModel = PersonListViewModel(repository)
        viewModel.uiState.first { it is PersonListUiState.Error }

        viewModel.refresh()

        assertEquals(PersonListUiState.Success(listOf(JANE)), viewModel.uiState.first { it is PersonListUiState.Success })
    }

    @Test
    fun failedRefreshKeepsCachedPeopleAndReportsError() = runTest {
        responses += ASTROS
        responses += null
        val viewModel = PersonListViewModel(repository)
        viewModel.uiState.first { it is PersonListUiState.Success }

        viewModel.refresh()

        val state = viewModel.uiState.first { (it as? PersonListUiState.Success)?.refreshError != null }
        assertEquals(listOf(JANE), (state as PersonListUiState.Success).result)
    }

    private companion object {
        const val ASTROS = """{"message":"success","number":1,"people":[{"craft":"ISS","name":"Jane Doe","nationality":"Irish"}]}"""
        val JANE = Assignment(craft = "ISS", name = "Jane Doe", nationality = "Irish")
    }
}
