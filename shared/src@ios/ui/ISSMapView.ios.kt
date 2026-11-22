package dev.johnoreilly.peopleinspace.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import dev.johnoreilly.peopleinspace.IssPosition
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKPointAnnotation

/** MapKit directly from Kotlin, so no Swift view factory is needed. */
@OptIn(ExperimentalForeignApi::class)
@Composable
internal actual fun ISSMapView(modifier: Modifier, position: IssPosition) {
    val annotation = remember { MKPointAnnotation().apply { setTitle("ISS") } }
    UIKitView(
        factory = { MKMapView().apply { addAnnotation(annotation) } },
        modifier = modifier,
        update = { mapView ->
            val coordinate = CLLocationCoordinate2DMake(position.latitude, position.longitude)
            annotation.setCoordinate(coordinate)
            mapView.setRegion(MKCoordinateRegionMakeWithDistance(coordinate, 5_000_000.0, 5_000_000.0), animated = true)
        },
        properties = UIKitInteropProperties(interactionMode = UIKitInteropInteractionMode.NonCooperative),
    )
}
