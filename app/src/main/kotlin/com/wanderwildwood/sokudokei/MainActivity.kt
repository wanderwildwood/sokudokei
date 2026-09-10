package com.wanderwildwood.sokudokei

import android.Manifest
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mudita.mmd.ThemeMMD
import com.wanderwildwood.sokudokei.meter.MeterViewModel
import com.wanderwildwood.sokudokei.ui.BigSpeedScreen
import com.wanderwildwood.sokudokei.ui.MeterScreen
import com.wanderwildwood.sokudokei.ui.SettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The screen stays on while this app is in front.
        //
        // A speedometer is read *while doing something else*, in glances, with both hands
        // busy — which is the one situation where the ordinary screen timeout is not a
        // sensible default but a fault. Waking a phone propped on a dashboard to find out
        // how fast you are going is worse than not having the app.
        //
        // This is the window flag, not a WAKE_LOCK: it needs no permission, and Android
        // drops it by itself the moment the window loses focus, so it cannot be left on
        // by accident or outlive the app.
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            ThemeMMD {
                Speedometer()
            }
        }
    }
}

/** Which of the three screens is up. */
private enum class Screen { METER, BIG, SETTINGS }

@Composable
private fun Speedometer(viewModel: MeterViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var screen by remember { mutableStateOf(Screen.METER) }

    val ask = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        viewModel::permissionAnswered,
    )

    // The GPS and the barometer run only while this app is in front. Both are switched on
    // at resume rather than once at startup, because the location permission, the phone's
    // location switch and the provider list can all change while the app is in the
    // background, and each of them would otherwise leave a dead screen behind.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.refresh()
                Lifecycle.Event.ON_PAUSE -> viewModel.pause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    when (screen) {
        Screen.METER -> MeterScreen(
            state = state,
            onSettings = { screen = Screen.SETTINGS },
            onFullScreen = { screen = Screen.BIG },
            onAllow = { ask.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
        )

        Screen.BIG -> BigSpeedScreen(
            state = state,
            onClose = { screen = Screen.METER },
        )

        Screen.SETTINGS -> SettingsScreen(
            state = state,
            onClose = { screen = Screen.METER },
            onSpeedUnit = viewModel::nextSpeedUnit,
            onAltitudeUnit = viewModel::nextAltitudeUnit,
            onPressureUnit = viewModel::nextPressureUnit,
            onProvider = viewModel::chooseProvider,
        )
    }
}
