package dev.johnoreilly.peopleinspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PersonListUiState {
    data object Loading : PersonListUiState
    data class Error(val message: String) : PersonListUiState
    data class Success(val result: List<Assignment>) : PersonListUiState
}

class PersonListViewModel(private val repository: PeopleInSpaceRepository) : ViewModel() {
    private val refreshError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PersonListUiState> =
        combine(repository.people, refreshError) { people, error ->
            when {
                people.isNotEmpty() -> PersonListUiState.Success(people)
                error != null -> PersonListUiState.Error(error)
                else -> PersonListUiState.Loading
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PersonListUiState.Loading)

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            refreshError.value = null
            try {
                repository.refreshPeople()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                refreshError.value = e.message ?: "Unknown error"
            }
        }
    }
}

class ISSPositionViewModel(private val repository: PeopleInSpaceRepository) : ViewModel() {
    val position: StateFlow<IssPosition> = flow {
        while (true) {
            try {
                emit(repository.fetchIssPosition())
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // keep showing the last position; try again on the next poll
            }
            delay(10_000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IssPosition(0.0, 0.0))
}
