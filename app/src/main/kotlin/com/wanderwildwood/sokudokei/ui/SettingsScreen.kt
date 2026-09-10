package com.wanderwildwood.sokudokei.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.text.TextMMD
import com.mudita.mmd.components.top_app_bar.TopAppBarMMD
import com.wanderwildwood.sokudokei.meter.MeterState

/**
 * The four things there are to set.
 *
 * The three units cycle in place on a press: each has two to four values, all of which
 * fit on the row already, so a picker would be two full repaints to choose between things
 * the screen is showing. The provider is a picker because its list is whatever this
 * particular handset happens to offer, and it can be long.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: MeterState,
    onClose: () -> Unit,
    onSpeedUnit: () -> Unit,
    onAltitudeUnit: () -> Unit,
    onPressureUnit: () -> Unit,
    onProvider: (String) -> Unit,
) {
    var aboutOpen by remember { mutableStateOf(false) }
    var providerOpen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopAppBarMMD(
                title = { TextMMD(text = "Settings", fontSize = 24.sp) },
                navigationIcon = { BarButton(Icons.Close, "Close", onClose) },
                actions = { BarButton(Icons.Info, "About", { aboutOpen = true }) },
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
            Spacer(Modifier.height(12.dp))

            Setting("Speed", state.speedUnit.label, onSpeedUnit)
            Setting("Altitude", state.altitudeUnit.label, onAltitudeUnit)

            // No barometer, no rows about one. A permanent dash beside a setting that
            // cannot do anything is worse than the setting not being there.
            if (state.hasBarometer) {
                Setting("Air pressure", state.pressureUnit.label, onPressureUnit)
            }

            Setting(
                title = "Read position from",
                value = state.provider.ifEmpty { "Not set" },
                onClick = { providerOpen = true },
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    if (aboutOpen) AboutDialog(onDismiss = { aboutOpen = false })

    if (providerOpen) {
        ProviderDialog(
            providers = state.providers,
            chosen = state.provider,
            onChoose = {
                onProvider(it)
                providerOpen = false
            },
            onDismiss = { providerOpen = false },
        )
    }
}

@Composable
private fun Setting(title: String, value: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
    ) {
        TextMMD(text = title, fontSize = 18.sp)
        TextMMD(text = value, fontSize = 14.sp)
    }
}

/**
 * Which of the phone's providers to read from.
 *
 * The names are the platform's own — "gps", "network", "passive" — and are left as they
 * are rather than translated into friendlier words, because a friendlier word would be a
 * guess about what a given handset means by them.
 */
@Composable
private fun ProviderDialog(
    providers: List<String>,
    chosen: String,
    onChoose: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    EInkDialog(onDismiss = onDismiss) {
        TextMMD(text = "Read position from", fontSize = 20.sp)
        Spacer(Modifier.height(10.dp))

        if (providers.isEmpty()) {
            TextMMD(text = "This phone offers none.", fontSize = 14.sp)
        } else {
            providers.forEach { provider ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChoose(provider) }
                        .padding(vertical = 12.dp),
                ) {
                    TextMMD(
                        text = if (provider == chosen) "$provider  ·  in use" else provider,
                        fontSize = 16.sp,
                    )
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        OutlinedButtonMMD(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) { TextMMD(text = "Close", fontSize = 15.sp) }
    }
}
