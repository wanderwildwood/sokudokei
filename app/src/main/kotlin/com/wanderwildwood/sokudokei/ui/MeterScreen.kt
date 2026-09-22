package com.wanderwildwood.sokudokei.ui

import androidx.annotation.StringRes
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.divider.HorizontalDividerMMD
import com.mudita.mmd.components.lazy.LazyColumnMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import com.wanderwildwood.sokudokei.R
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
                title = { TextMMD(text = stringResource(R.string.meter_title)) },
                actions = { BarButton(Icons.Settings, stringResource(R.string.meter_cd_settings), onSettings) },
            )
        },
    ) { contentPadding ->
        // MMD's list, not a scrolling Column: it steps four rows to a swipe and stops, and it
        // brings the chevron rail at both ends. A settings screen that coasts was the one
        // screen in the app that did not behave like the phone it is on.
        LazyColumnMMD(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
                .padding(horizontal = 20.dp),
        ) {
            item {
                Speed(state, onFullScreen)
            }
            item {

                if (state.trouble != Trouble.NONE) {
                    Trouble(state.trouble, onAllow)
                }
            }
            item {

                Spacer(Modifier.height(8.dp))
            }
            item {
                HorizontalDividerMMD()
            }
            item {

                Altitude(state)
            }
            item {

                if (state.fix != null) {
                    HorizontalDividerMMD()
                    Position(state)
                }
            }
            item {

                if (state.hasBarometer) {
                    HorizontalDividerMMD()
                    Air(state)
                }
            }
            item {

                Spacer(Modifier.height(24.dp))
            }
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
        TextMMD(text = stringResource(state.speedUnit.labelRes), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun Altitude(state: MeterState) {
    val fix = state.fix
    val unit = state.altitudeUnit

    Reading(
        label = stringResource(R.string.meter_altitude),
        value = if (fix == null || !fix.hasAltitude) "–"
        else unit.from(fix.altitudeMetres).toInt().toString(),
        unit = stringResource(unit.labelRes),
        // The GPS gives height above the WGS84 ellipsoid, and on this phone Android
        // cannot convert it: the conversion arrived in API 34 and the Kompakt is 31.
        // Saying which datum it is costs one line and stops the number being read as a
        // height above the sea, which in most places it is not, by tens of metres.
        note = stringResource(R.string.meter_altitude_note),
    )
}

@Composable
private fun Position(state: MeterState) {
    val fix = state.fix ?: return

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp)) {
        TextMMD(text = stringResource(R.string.meter_position), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        TextMMD(
            text = coordinate(fix.latitude, R.string.meter_position_north, R.string.meter_position_south),
            style = MaterialTheme.typography.bodySmall,
        )
        TextMMD(
            text = coordinate(fix.longitude, R.string.meter_position_east, R.string.meter_position_west),
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(4.dp))
        TextMMD(
            text = if (fix.accuracyMetres.isNaN()) fix.provider
            else stringResource(
                R.string.meter_position_accuracy,
                fix.provider,
                state.altitudeUnit.from(fix.accuracyMetres.toDouble()).toInt(),
                stringResource(state.altitudeUnit.labelRes),
            ),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

/** One coordinate in words: the side of zero it lies on, then degrees, minutes and seconds. */
@Composable
private fun coordinate(degrees: Double, @StringRes positive: Int, @StringRes negative: Int): String {
    val dms = degreesMinutesSeconds(degrees)
    return stringResource(
        R.string.meter_position_dms,
        stringResource(if (dms.negative) negative else positive),
        dms.degrees.toString(),
        dms.minutes.toString(),
        dms.secondsText,
    )
}

@Composable
private fun Air(state: MeterState) {
    val unit = state.pressureUnit
    val hPa = state.hectopascals

    Reading(
        label = stringResource(R.string.meter_air_pressure),
        value = if (hPa.isNaN()) "–"
        else String.format("%.${unit.decimals}f", unit.from(hPa)),
        unit = stringResource(unit.labelRes),
    )

    HorizontalDividerMMD()

    val seaLevel = state.seaLevelHectopascals
    Reading(
        label = stringResource(R.string.meter_sea_level),
        value = if (seaLevel.isNaN()) "–"
        else String.format("%.${unit.decimals}f", unit.from(seaLevel)),
        unit = stringResource(unit.labelRes),
        note = if (seaLevel.isNaN()) stringResource(R.string.meter_sea_level_note) else null,
    )

    HorizontalDividerMMD()

    val pressureAltitude = state.pressureAltitudeMetres
    Reading(
        label = stringResource(R.string.meter_pressure_altitude),
        value = if (pressureAltitude.isNaN()) "–"
        else state.altitudeUnit.from(pressureAltitude).toInt().toString(),
        unit = stringResource(state.altitudeUnit.labelRes),
        // Worth saying plainly: this is a standard-atmosphere height, so on a low
        // pressure day it disagrees with the GPS by a hundred metres and neither of
        // them is broken.
        note = stringResource(R.string.meter_pressure_altitude_note),
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
            TextMMD(text = label, style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.Bottom) {
                TextMMD(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Medium)
                Spacer(Modifier.size(6.dp))
                TextMMD(text = unit, style = MaterialTheme.typography.labelSmall)
            }
        }
        if (note != null) {
            Spacer(Modifier.height(2.dp))
            TextMMD(text = note, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun Trouble(trouble: Trouble, onAllow: () -> Unit) {
    val message = when (trouble) {
        Trouble.NO_PERMISSION -> stringResource(R.string.meter_trouble_no_permission)
        Trouble.LOCATION_OFF -> stringResource(R.string.meter_trouble_location_off)
        Trouble.NO_PROVIDER -> stringResource(R.string.meter_trouble_no_provider)
        Trouble.PROVIDER_OFF -> stringResource(R.string.meter_trouble_provider_off)
        Trouble.NONE -> return
    }

    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        TextMMD(text = message, style = MaterialTheme.typography.labelSmall)
        if (trouble == Trouble.NO_PERMISSION) {
            Spacer(Modifier.height(10.dp))
            OutlinedButtonMMD(
                onClick = onAllow,
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) { TextMMD(text = stringResource(R.string.meter_allow_location), style = MaterialTheme.typography.bodySmall) }
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
