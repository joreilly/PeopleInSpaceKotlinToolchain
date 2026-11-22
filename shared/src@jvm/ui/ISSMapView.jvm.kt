package dev.johnoreilly.peopleinspace.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.johnoreilly.peopleinspace.IssPosition
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.skia.Image
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.tan

private const val ZOOM = 4 // same zoom as the Android (osmdroid) map
private const val TILES_PER_SIDE = 1 shl ZOOM
private const val TILE_DP = 256
private val OsmWater = Color(0xFFAAD3DF)

private data class Tile(val x: Int, val y: Int)

/**
 * Desktop has no native map SDK, so this draws OpenStreetMap tiles on a Compose [Canvas],
 * centred on the ISS, with a marker at its position.
 */
@Composable
internal actual fun ISSMapView(modifier: Modifier, position: IssPosition) {
    val client = remember { HttpClient() }
    DisposableEffect(client) { onDispose { client.close() } }
    val tiles = remember { mutableStateMapOf<Tile, ImageBitmap>() }

    BoxWithConstraints(modifier.fillMaxSize().clipToBounds().background(OsmWater)) {
        val tileSize = with(LocalDensity.current) { TILE_DP.dp.roundToPx() }
        val widthPx = constraints.maxWidth
        val heightPx = constraints.maxHeight

        // ISS position in fractional tile coordinates (Web Mercator), and the map's top-left corner
        val centerX = (position.longitude + 180.0) / 360.0 * TILES_PER_SIDE
        val latRad = position.latitude * PI / 180.0
        val centerY = (1.0 - ln(tan(latRad) + 1.0 / cos(latRad)) / PI) / 2.0 * TILES_PER_SIDE
        val originX = centerX - widthPx / 2.0 / tileSize
        val originY = centerY - heightPx / 2.0 / tileSize

        val visible = buildList {
            for (tx in floor(originX).toInt()..floor(originX + widthPx.toDouble() / tileSize).toInt()) {
                for (ty in floor(originY).toInt()..floor(originY + heightPx.toDouble() / tileSize).toInt()) {
                    if (ty in 0 until TILES_PER_SIDE) add(Tile(tx, ty))
                }
            }
        }

        LaunchedEffect(visible) {
            // x wraps around the antimeridian
            visible.map { Tile(it.x.mod(TILES_PER_SIDE), it.y) }.distinct()
                .filter { it !in tiles }
                .forEach { tile ->
                    launch {
                        try {
                            val bytes = client.get("https://tile.openstreetmap.org/$ZOOM/${tile.x}/${tile.y}.png") {
                                // OSM's tile usage policy requires an identifying User-Agent
                                header("User-Agent", "PeopleInSpaceKotlinToolchain/1.0 (+https://github.com/joreilly/PeopleInSpaceKotlinToolchain)")
                            }.body<ByteArray>()
                            tiles[tile] = Image.makeFromEncoded(bytes).toComposeImageBitmap()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (_: Exception) {
                            // leave the water-coloured background; retried when the visible tiles change
                        }
                    }
                }
        }

        Canvas(Modifier.fillMaxSize()) {
            visible.forEach { tile ->
                val image = tiles[Tile(tile.x.mod(TILES_PER_SIDE), tile.y)] ?: return@forEach
                drawImage(
                    image = image,
                    dstOffset = IntOffset(
                        ((tile.x - originX) * tileSize).toInt(),
                        ((tile.y - originY) * tileSize).toInt(),
                    ),
                    dstSize = IntSize(tileSize, tileSize),
                )
            }
        }

        // The map is centred on the ISS, so the pin's tip sits at the centre
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "ISS",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.align(Alignment.Center).offset(y = (-20).dp).size(40.dp),
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
