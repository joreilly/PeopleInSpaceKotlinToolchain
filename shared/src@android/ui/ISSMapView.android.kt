package dev.johnoreilly.peopleinspace.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.utsman.osmandcompose.CameraProperty
import com.utsman.osmandcompose.CameraState
import com.utsman.osmandcompose.DefaultMapProperties
import com.utsman.osmandcompose.Marker
import com.utsman.osmandcompose.MarkerState
import com.utsman.osmandcompose.OpenStreetMap
import dev.johnoreilly.peopleinspace.IssPosition
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import java.io.File

// osm-android-compose marks its map content with its own "OsmAnd Composable" applier target, so the
// compiler flags OpenStreetMap/Marker as applier mismatches even though this is the library's intended
// use (the original PeopleInSpace has the same warnings).
@Suppress("COMPOSE_APPLIER_CALL_MISMATCH")
@Composable
internal actual fun ISSMapView(modifier: Modifier, position: IssPosition) {
    val context = LocalContext.current
    remember {
        // OSM tile servers require an identifying user agent
        Configuration.getInstance().userAgentValue = context.packageName
        Configuration.getInstance().osmdroidTileCache = File(context.cacheDir, "osm").also { it.mkdirs() }
    }

    val geoPoint = GeoPoint(position.latitude, position.longitude)
    val cameraState = remember(position) { CameraState(CameraProperty().apply { this.geoPoint = geoPoint; zoom = 4.0 }) }
    val markerState = remember(position) { MarkerState(geoPoint, 0.0f) }

    Surface(modifier = modifier.fillMaxSize()) {
        OpenStreetMap(
            cameraState = cameraState,
            properties = DefaultMapProperties.copy(minZoomLevel = 4.0),
        ) {
            Marker(state = markerState, title = "ISS")
        }
    }
}
