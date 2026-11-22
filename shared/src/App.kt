package dev.johnoreilly.peopleinspace

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.jetbrains.compose.reload.DevelopmentEntryPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@DevelopmentEntryPoint
fun App(viewModel: PeopleInSpaceViewModel = viewModel { PeopleInSpaceViewModel() }) {
    val uiState by viewModel.uiState.collectAsState()
    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("People In Space") }) }) { padding ->
            Column(Modifier.padding(padding)) {
                val position = uiState.issPosition
                Text(
                    text = if (position == null) "ISS position: loading…"
                    else "ISS position: ${position.latitude.format()}, ${position.longitude.format()}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp),
                )
                uiState.error?.let { Text("Error: $it", Modifier.padding(horizontal = 16.dp)) }
                LazyColumn {
                    items(uiState.people) { person ->
                        ListItem(
                            headlineContent = { Text(person.name) },
                            supportingContent = { Text(person.craft) },
                        )
                    }
                }
            }
        }
    }
}

private fun Double.format(): String = (kotlin.math.round(this * 1000) / 1000).toString()
