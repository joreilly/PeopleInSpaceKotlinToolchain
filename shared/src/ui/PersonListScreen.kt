package dev.johnoreilly.peopleinspace.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.johnoreilly.peopleinspace.Assignment
import dev.johnoreilly.peopleinspace.PersonListUiState
import dev.johnoreilly.peopleinspace.PersonListViewModel
import org.koin.compose.viewmodel.koinViewModel

const val PersonListTag = "PersonList"

@Composable
fun PersonListRoute(navigateToPerson: (Assignment) -> Unit) {
    val viewModel: PersonListViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    PersonListScreen(uiState, isRefreshing, navigateToPerson, onRefresh = { viewModel.refresh() })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonListScreen(
    uiState: PersonListUiState,
    isRefreshing: Boolean,
    navigateToPerson: (Assignment) -> Unit,
    onRefresh: () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "People In Space") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.semantics { contentDescription = "PeopleInSpace" },
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        when (uiState) {
            is PersonListUiState.Error -> CenteredMessage(
                modifier = Modifier.padding(innerPadding),
                title = "Error loading astronauts",
                message = uiState.message,
                tint = MaterialTheme.colorScheme.error,
                onRetry = onRefresh,
                isRetrying = isRefreshing,
            )

            is PersonListUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
            }

            is PersonListUiState.Success -> PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = Modifier.padding(innerPadding),
            ) {
                // A LazyColumn even when empty, so pull-to-refresh still works
                LazyColumn(
                    modifier = Modifier
                        .testTag(PersonListTag)
                        .consumeWindowInsets(innerPadding)
                        .fillMaxSize()
                ) {
                    uiState.refreshError?.let { error ->
                        item {
                            Text(
                                text = "Couldn't refresh: $error",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                            )
                        }
                    }
                    if (uiState.result.isEmpty()) {
                        item {
                            CenteredMessage(
                                title = "No astronauts found",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.fillParentMaxSize(),
                            )
                        }
                    }
                    itemsIndexed(uiState.result) { index, person ->
                        PersonView(index, person, navigateToPerson)
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredMessage(
    title: String,
    tint: Color,
    modifier: Modifier = Modifier,
    message: String? = null,
    onRetry: (() -> Unit)? = null,
    isRetrying: Boolean = false,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Icon(imageVector = Icons.Default.Person, contentDescription = null, modifier = Modifier.size(48.dp), tint = tint)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, color = tint)
            if (message != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (onRetry != null) {
                Spacer(modifier = Modifier.height(16.dp))
                if (isRetrying) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                } else {
                    Button(onClick = onRetry) { Text("Retry") }
                }
            }
        }
    }
}

@Composable
fun PersonView(index: Int, person: Assignment, personSelected: (person: Assignment) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = { personSelected(person) }),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val personImageUrl = person.personImageUrl ?: ""
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center,
            ) {
                if (personImageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = personImageUrl,
                        contentDescription = person.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(64.dp).clip(CircleShape),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }

            Spacer(modifier = Modifier.size(16.dp))

            Column {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("person$index"),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = person.craft,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (person.nationality.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = person.nationality,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
