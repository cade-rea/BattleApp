package com.caderea.battleapp.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.caderea.battleapp.view.map.MapView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MapActivity : ComponentActivity() {

    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mapView = MapView(this)
        setContentView(mapView)

        // the map only ticks while it's on screen
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (true) {
                    delay(MAP_TICK_MS)
                    mapView.moveCircle()
                }
            }
        }
    }

    private companion object {
        const val MAP_TICK_MS = 5000L
    }
}
