package dev.johnoreilly.peopleinspace.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import coil3.compose.LocalPlatformContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.network.NetworkHeaders
import coil3.network.httpHeaders
import coil3.request.ImageRequest
import dev.johnoreilly.peopleinspace.IssPosition
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.min
import kotlin.math.tan

private const val ZOOM = 4 // same zoom as the Android (osmdroid) map
private const val TILES_PER_SIDE = 1 shl ZOOM
private val TileSize = 256.dp
private val PinSize = 40.dp
private val OsmWater = Color(0xFFAAD3DF)

// OSM's tile usage policy requires an identifying User-Agent
private val OsmHeaders = NetworkHeaders.Builder()
    .set("User-Agent", "PeopleInSpaceKotlinToolchain/1.0 (+https://github.com/joreilly/PeopleInSpaceKotlinToolchain)")
    .build()

/**
 * Desktop has no native map SDK, so this lays out OpenStreetMap tiles centred on the ISS, with a
 * marker at its position. Tiles are loaded with Coil (the same loader as the astronaut photos), which
 * decodes off the main thread and caches them in memory and on disk.
 */
@Composable
internal actual fun ISSMapView(modifier: Modifier, position: IssPosition) {
    BoxWithConstraints(modifier.fillMaxSize().clipToBounds().background(OsmWater)) {
        // ISS position in fractional tile coordinates (Web Mercator), and the map's top-left corner
        val centerX = (position.longitude + 180.0) / 360.0 * TILES_PER_SIDE
        val latRad = position.latitude * PI / 180.0
        val centerY = (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * TILES_PER_SIDE
        val tilesAcross = maxWidth / TileSize
        val tilesDown = maxHeight / TileSize
        val originX = centerX - tilesAcross / 2.0
        val originY = centerY - tilesDown / 2.0

        for (tx in floor(originX).toInt()..floor(originX + tilesAcross).toInt()) {
            for (ty in floor(originY).toInt()..floor(originY + tilesDown).toInt()) {
                if (ty !in 0 until TILES_PER_SIDE) continue
                key(tx, ty) {
                    MapTile(
                        x = tx.mod(TILES_PER_SIDE), // wraps around the antimeridian
                        y = ty,
                        modifier = Modifier
                            .offset { IntOffset(((tx - originX) * TileSize.roundToPx()).toInt(), ((ty - originY) * TileSize.roundToPx()).toInt()) }
                            .size(TileSize),
                    )
                }
            }
        }

        // The map is centred on the ISS. LocationOn's tip is at y=22 of its 24-unit viewport,
        // so shift the icon up until that point sits on the centre.
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "ISS",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = -(PinSize * (22f / 24f) - PinSize / 2))
                .size(PinSize),
        )

        Text(
            text = "© OpenStreetMap contributors",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(Color.White.copy(alpha = 0.7f))
                .padding(horizontal = 4.dp, vertical = 2.dp),
        )
    }
}

/** One map tile. Coil doesn't retry failed loads, so a failed tile retries with backoff (5s, 10s, ... up to 60s). */
@Composable
private fun MapTile(x: Int, y: Int, modifier: Modifier) {
    var attempt by remember { mutableIntStateOf(0) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(failed) {
        if (failed) {
            delay(min(5_000L shl min(attempt, 4), 60_000L))
            failed = false
            attempt++
        }
    }

    val context = LocalPlatformContext.current
    key(attempt) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data("https://tile.openstreetmap.org/$ZOOM/$x/$y.png")
                .httpHeaders(OsmHeaders)
                .build(),
            contentDescription = null,
            onError = { failed = true },
            modifier = modifier,
        )
    }
}
