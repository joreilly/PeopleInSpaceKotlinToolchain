package dev.johnoreilly.peopleinspace.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.johnoreilly.peopleinspace.ISSPositionViewModel
import dev.johnoreilly.peopleinspace.IssPosition
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.pow
import kotlin.math.round

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ISSPositionRoute(viewModel: ISSPositionViewModel = koinViewModel()) {
    val position by viewModel.position.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "ISS Position") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.semantics { contentDescription = "ISSPosition" },
            )
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            ISSPositionContent(position)
        }
    }
}

@Composable
fun ISSPositionContent(position: IssPosition) {
    Column {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ISS Current Location",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    CoordinateDisplay("Latitude", position.latitude.round(4).toString(), Modifier.weight(1f))
                    Spacer(Modifier.width(16.dp))
                    CoordinateDisplay("Longitude", position.longitude.round(4).toString(), Modifier.weight(1f))
                }
            }
        }

        // Map view takes the remaining space
        ISSMapView(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(8.dp)),
            position,
        )
    }
}

@Composable
internal fun CoordinateDisplay(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

private fun Double.round(decimals: Int): Double {
    val multiplier = 10.0.pow(decimals)
    return round(this * multiplier) / multiplier
}

/** OpenStreetMap on Android (osmdroid) and desktop (tiles drawn on a Canvas), MapKit on iOS. */
@Composable
internal expect fun ISSMapView(modifier: Modifier, position: IssPosition)
