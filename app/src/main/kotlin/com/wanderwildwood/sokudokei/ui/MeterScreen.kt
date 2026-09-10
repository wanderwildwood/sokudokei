package com.wanderwildwood.sokudokei.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import com.wanderwildwood.sokudokei.core.degreesMinutesSeconds
import com.wanderwildwood.sokudokei.meter.MeterState
import com.wanderwildwood.sokudokei.meter.Trouble

/**
 * Everything the phone can tell you about how you are moving, on one screen.
 *
 * Speed is the large number because it is the one read at a glance while doing something
 * else; the rest are rows because they are read deliberately or not at all. A press on
 * the speed opens the same number filling the screen, which is the only way to read it
 * propped on a dashboard or a handlebar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeterScreen(
    state: MeterState,
    onSettings: () -> Unit,
    onFullScreen: () -> Unit,
    onAllow: () -> Unit,
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBarMMD(
                title = { TextMMD(text = "Speedometer", fontSize = 24.sp) },
                actions = { BarButton(Icons.Settings, "Settings", onSettings) },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Speed(state, onFullScreen)

            if (state.trouble != Trouble.NONE) {
                Trouble(state.trouble, onAllow)
            }

            Spacer(Modifier.height(8.dp))
            HorizontalDividerMMD()

            Altitude(state)

            if (state.fix != null) {
                HorizontalDividerMMD()
                Position(state)
            }

            if (state.hasBarometer) {
                HorizontalDividerMMD()
                Air(state)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Speed(state: MeterState, onFullScreen: () -> Unit) {
    val fix = state.fix
    val reading = when {
        fix == null || !fix.hasSpeed -> "–"
        else -> state.speedUnit.from(fix.speedMetresPerSecond).toInt().toString()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = fix != null, onClick = onFullScreen)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TextMMD(text = reading, fontSize = 84.sp, fontWeight = FontWeight.Medium)
        TextMMD(text = state.speedUnit.label, fontSize = 18.sp)
    }
}

@Composable
private fun Altitude(state: MeterState) {
    val fix = state.fix
    val unit = state.altitudeUnit

    Reading(
        label = "Altitude",
        value = if (fix == null || !fix.hasAltitude) "–"
        else unit.from(fix.altitudeMetres).toInt().toString(),
        unit = unit.label,
        // The GPS gives height above the WGS84 ellipsoid, and on this phone Android
        // cannot convert it: the conversion arrived in API 34 and the Kompakt is 31.
        // Saying which datum it is costs one line and stops the number being read as a
        // height above the sea, which in most places it is not, by tens of metres.
        note = "Above the WGS84 ellipsoid, not sea level.",
    )
}

@Composable
private fun Position(state: MeterState) {
    val fix = state.fix ?: return

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
        TextMMD(text = "Position", fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        TextMMD(
            text = degreesMinutesSeconds(fix.latitude, "N", "S"),
            fontSize = 15.sp,
        )
        TextMMD(
            text = degreesMinutesSeconds(fix.longitude, "E", "W"),
            fontSize = 15.sp,
        )
        Spacer(Modifier.height(4.dp))
        TextMMD(
            text = if (fix.accuracyMetres.isNaN()) fix.provider
            else "${fix.provider}, to about ${
                state.altitudeUnit.from(fix.accuracyMetres.toDouble()).toInt()
            } ${state.altitudeUnit.label}",
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun Air(state: MeterState) {
    val unit = state.pressureUnit
    val hPa = state.hectopascals

    Reading(
        label = "Air pressure",
        value = if (hPa.isNaN()) "–"
        else String.format("%.${unit.decimals}f", unit.from(hPa)),
        unit = unit.label,
    )

    HorizontalDividerMMD()

    val seaLevel = state.seaLevelHectopascals
    Reading(
        label = "At sea level",
        value = if (seaLevel.isNaN()) "–"
        else String.format("%.${unit.decimals}f", unit.from(seaLevel)),
        unit = unit.label,
        note = if (seaLevel.isNaN()) "Needs an altitude from the GPS." else null,
    )

    HorizontalDividerMMD()

    val pressureAltitude = state.pressureAltitudeMetres
    Reading(
        label = "Pressure altitude",
        value = if (pressureAltitude.isNaN()) "–"
        else state.altitudeUnit.from(pressureAltitude).toInt().toString(),
        unit = state.altitudeUnit.label,
        // Worth saying plainly: this is a standard-atmosphere height, so on a low
        // pressure day it disagrees with the GPS by a hundred metres and neither of
        // them is broken.
        note = "What the height would be on a standard day.",
    )
}

/** A label, a number with its unit, and a note only where a label cannot carry it. */
@Composable
private fun Reading(label: String, value: String, unit: String, note: String? = null) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            TextMMD(text = label, fontSize = 18.sp)
            Row(verticalAlignment = Alignment.Bottom) {
                TextMMD(text = value, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.size(6.dp))
                TextMMD(text = unit, fontSize = 14.sp)
            }
        }
        if (note != null) {
            Spacer(Modifier.height(2.dp))
            TextMMD(text = note, fontSize = 13.sp)
        }
    }
}

@Composable
private fun Trouble(trouble: Trouble, onAllow: () -> Unit) {
    val message = when (trouble) {
        Trouble.NO_PERMISSION -> "This needs the location permission to read a speed."
        Trouble.LOCATION_OFF -> "Location is switched off for the whole phone."
        Trouble.NO_PROVIDER -> "No location provider chosen yet. Pick one in settings."
        Trouble.PROVIDER_OFF -> "The chosen provider is switched off."
        Trouble.NONE -> return
    }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        TextMMD(text = message, fontSize = 14.sp)
        if (trouble == Trouble.NO_PERMISSION) {
            Spacer(Modifier.height(10.dp))
            OutlinedButtonMMD(
                onClick = onAllow,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { TextMMD(text = "Allow location", fontSize = 15.sp) }
        }
    }
}

@Composable
internal fun BarButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.size(48.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(22.dp),
        )
    }
}
