package dev.johnoreilly.peopleinspace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface PersonListUiState {
    data object Loading : PersonListUiState
    /** Nothing cached and the fetch failed. */
    data class Error(val message: String) : PersonListUiState
    /** [refreshError] is set when a refresh failed but cached people are still shown. */
    data class Success(val result: List<Assignment>, val refreshError: String? = null) : PersonListUiState
}

class PersonListViewModel(private val repository: PeopleInSpaceRepository) : ViewModel() {
    private data class RefreshState(
        val error: String? = null,
        /**
         * The last successful fetch returned nobody, so an empty list means "nobody in space" rather
         * than "not loaded yet". Keyed on the fetch result, not just "a fetch succeeded", so the UI
         * doesn't flash an empty list before the database emits the rows that fetch just wrote.
         */
        val fetchedEmpty: Boolean = false,
    )

    private val refreshState = MutableStateFlow(RefreshState())
    private var refreshJob: Job? = null

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val uiState: StateFlow<PersonListUiState> =
        combine(repository.people, refreshState) { people, refresh ->
            when {
                people.isNotEmpty() || refresh.fetchedEmpty -> PersonListUiState.Success(people, refresh.error)
                refresh.error != null -> PersonListUiState.Error(refresh.error)
                else -> PersonListUiState.Loading
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PersonListUiState.Loading)

    init {
        refresh()
    }

    /** Fetches the crew; ignored while a refresh is already running. */
    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            _isRefreshing.value = true
            refreshState.update { it.copy(error = null) }
            try {
                val people = repository.refreshPeople()
                refreshState.update { it.copy(fetchedEmpty = people.isEmpty()) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                refreshState.update { it.copy(error = e.message ?: "Unknown error") }
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}

/** [position] is null until the first fetch succeeds; [error] is the last failure, if any. */
data class ISSPositionUiState(val position: IssPosition? = null, val error: String? = null)

class ISSPositionViewModel(private val repository: PeopleInSpaceRepository) : ViewModel() {
    val uiState: StateFlow<ISSPositionUiState> = flow {
        var state = ISSPositionUiState()
        while (true) {
            state = try {
                ISSPositionUiState(position = repository.fetchIssPosition())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // keep showing the last known position; try again on the next poll
                state.copy(error = e.message ?: "Unknown error")
            }
            emit(state)
            delay(10_000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ISSPositionUiState())
}
