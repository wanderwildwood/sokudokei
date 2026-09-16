package com.wanderwildwood.sokudokei.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.text.TextMMD
import com.wanderwildwood.sokudokei.meter.MeterState

/**
 * The speed, and nothing else, filling the screen.
 *
 * For a phone propped on a dashboard or clipped to a handlebar, where the reading is
 * taken in the half second it is safe to look away. No bar, no cog, no rows: anything
 * else on this screen is something the eye has to travel past.
 *
 * A press anywhere returns. There is no button because a target this size is easier to
 * hit without looking than a small one placed carefully, and there is nothing else here
 * that a press could otherwise have meant.
 */
@Composable
fun BigSpeedScreen(state: MeterState, onClose: () -> Unit) {
    val fix = state.fix
    val reading = when {
        fix == null || !fix.hasSpeed -> "–"
        else -> state.speedUnit.from(fix.speedMetresPerSecond).toInt().toString()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClose),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TextMMD(
                text = reading,
                fontSize = 160.sp,
                fontWeight = FontWeight.Medium,
            )
            TextMMD(text = state.speedUnit.label, style = MaterialTheme.typography.headlineLarge)
        }
    }
}
