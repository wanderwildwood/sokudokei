package com.wanderwildwood.sokudokei.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.text.TextMMD
import com.wanderwildwood.sokudokei.BuildConfig

/**
 * What this is, what it does with what it knows, and whose work it started from.
 *
 * The line about the position is here because a speedometer is a location app, and a
 * stranger has no way to tell from the outside whether one is sending its fixes anywhere.
 */
@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    EInkDialog(onDismiss = onDismiss) {
        TextMMD(
            text = "Speedometer ${BuildConfig.VERSION_NAME}",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
        )

        Spacer(Modifier.height(14.dp))
        TextMMD(
            text = "Your position never leaves the phone, and the app cannot reach the " +
                "network at all.",
            fontSize = 14.sp,
        )

        Spacer(Modifier.height(14.dp))
        TextMMD(
            text = "Speed and altitude come from the GPS, air pressure from the phone's " +
                "barometer. Nothing here is accurate enough to navigate by.",
            fontSize = 14.sp,
        )

        Spacer(Modifier.height(14.dp))
        TextMMD(
            text = "After Blue Square Speedometer by nhirokinet, Apache 2.0, whose unit " +
                "and atmosphere maths this carries.",
            fontSize = 14.sp,
        )

        Spacer(Modifier.height(14.dp))
        TextMMD(text = "GNU General Public License v3", fontSize = 14.sp)
        TextMMD(text = "Icons from Material Symbols, Apache 2.0", fontSize = 14.sp)

        Spacer(Modifier.height(14.dp))
        TextMMD(text = "github.com/wanderwildwood/sokudokei", fontSize = 14.sp)

        Spacer(Modifier.height(18.dp))
        OutlinedButtonMMD(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { TextMMD(text = "Close", fontSize = 15.sp) }
    }
}
