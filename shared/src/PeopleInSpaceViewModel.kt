package dev.johnoreilly.peopleinspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val people: List<Assignment> = emptyList(),
    val issPosition: IssPosition? = null,
    val error: String? = null,
)

class PeopleInSpaceViewModel(private val repository: PeopleInSpaceRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.people.collect { people -> _uiState.update { it.copy(people = people) } }
        }
        viewModelScope.launch {
            runCatchingNonCancel { repository.refreshPeople() }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
        viewModelScope.launch {
            while (true) {
                runCatchingNonCancel { repository.fetchIssPosition() }
                    .onSuccess { position -> _uiState.update { it.copy(issPosition = position) } }
                delay(10_000)
            }
        }
    }
}

private inline fun <T> runCatchingNonCancel(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
