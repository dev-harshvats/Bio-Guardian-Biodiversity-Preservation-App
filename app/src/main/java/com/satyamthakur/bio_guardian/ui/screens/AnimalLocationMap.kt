package com.satyamthakur.bio_guardian.ui.screens

import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.maps.android.heatmaps.HeatmapTileProvider

@Composable
fun AnimalLocationMap(coordinates: List<List<Double>>, animalName: String) {
    if (coordinates.isEmpty()) return

    val context = LocalContext.current
    val habitatPoints = coordinates.map { LatLng(it[0], it[1]) }

    val centerLat = habitatPoints.map { it.latitude }.average()
    val centerLng = habitatPoints.map { it.longitude }.average()
    val mapCenter = LatLng(centerLat, centerLng)

    val mapView = rememberMapViewWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
            update = { mapView ->
                mapView.getMapAsync { googleMap ->

                    googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN

                    googleMap.uiSettings.apply {
                        isZoomControlsEnabled = true
                        isCompassEnabled = true
                        isScrollGesturesEnabled = true
                        isZoomGesturesEnabled = true
                    }

                    googleMap.clear()

                    // Add markers
                    habitatPoints.forEach {
                        googleMap.addMarker(
                            MarkerOptions()
                                .position(it)
                                .title(animalName)
                        )
                    }

                    // Heatmap overlay
                    val heatmapTileProvider = HeatmapTileProvider.Builder()
                        .data(habitatPoints)
                        .build()

                    googleMap.addTileOverlay(
                        TileOverlayOptions().tileProvider(heatmapTileProvider)
                    )

                    // Prepare camera position
                    val cameraPosition = CameraPosition.Builder()
                        .target(mapCenter)
                        .zoom(5f)
                        .build()

                    // Smooth camera animation with duration and interpolator
                    googleMap.animateCamera(
                        CameraUpdateFactory.newCameraPosition(cameraPosition),
                        2000, // Duration in milliseconds (2 seconds)
                        object : GoogleMap.CancelableCallback {
                            override fun onFinish() {
                                // Optional: can trigger more animations or UI changes here
                            }

                            override fun onCancel() {
                                // Optional: handle cancellation
                            }
                        }
                    )
                }
            }
        )
    }
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            onCreate(null)
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    return mapView
}
