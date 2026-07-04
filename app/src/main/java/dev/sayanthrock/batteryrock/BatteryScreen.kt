package dev.sayanthrock.batteryrock

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val ScreenBackground = Color(0xFF05060A)
private val CardBackground = Color(0xFF111827)
private val CardBorder = Color(0x22FFFFFF)
private val PrimaryText = Color(0xFFF8FAFC)
private val SecondaryText = Color(0xFFA7B0C0)
private val MutedText = Color(0xFF64748B)
private val AccentGreen = Color(0xFF22C55E)
private val AccentAmber = Color(0xFFF59E0B)
private val AccentCyan = Color(0xFF38BDF8)

@Composable
fun BatteryScreen(viewModel: BatteryViewModel = viewModel()) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()

    DisposableEffect(context) {
        viewModel.start(context)
        onDispose { viewModel.stop(context) }
    }

    Surface(color = ScreenBackground) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(ScreenBackground, Color(0xFF0B1020))))
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BatteryHeader(state)
            BatteryPercentCard(state)
            DetailGrid(state)
            FooterNote()
        }
    }
}

@Composable
private fun BatteryHeader(state: BatteryUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Battery-Rock",
                color = PrimaryText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Modern battery monitor",
                color = SecondaryText,
                fontSize = 13.sp
            )
        }
        ChargingIndicator(isCharging = state.isCharging)
    }
}

@Composable
private fun BatteryPercentCard(state: BatteryUiState) {
    val statusColor by animateColorAsState(
        targetValue = when (state.status) {
            "Charging", "Full" -> AccentGreen
            "Discharging" -> AccentAmber
            else -> AccentCyan
        },
        label = "statusColor"
    )

    BatteryCard {
        Text(
            text = "${state.percentage}%",
            color = PrimaryText,
            fontSize = 72.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 76.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = state.status,
            color = statusColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Health: ${state.health}",
            color = SecondaryText,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun DetailGrid(state: BatteryUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Health", state.health, Modifier.weight(1f))
            StatCard("Temperature", state.temperature, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("Technology", state.technology, Modifier.weight(1f))
            StatCard("Voltage", state.voltage, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = CardDefaults.outlinedCardBorder().copy(width = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label.uppercase(),
                color = MutedText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                color = PrimaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun BatteryCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(28.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(28.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.Start,
        content = { content() }
    )
}

@Composable
private fun ChargingIndicator(isCharging: Boolean) {
    val transition = rememberInfiniteTransition(label = "chargePulse")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCharging) 1.16f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(
        modifier = Modifier
            .scale(pulse)
            .size(42.dp)
            .background(if (isCharging) AccentGreen.copy(alpha = 0.18f) else Color(0x0FFFFFFF), CircleShape)
            .border(1.dp, if (isCharging) AccentGreen else CardBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isCharging) "⚡" else "•",
            color = if (isCharging) AccentGreen else SecondaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FooterNote() {
    Text(
        text = "Live data from Android battery broadcast · No extra sensors required",
        color = MutedText,
        fontSize = 11.sp,
        modifier = Modifier.padding(top = 4.dp)
    )
}
