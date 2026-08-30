package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GundamBlue
import com.example.ui.theme.GundamBlueLight
import com.example.ui.theme.GundamDarkArmor
import com.example.ui.theme.GundamNavy
import com.example.ui.theme.GundamPanelLine
import com.example.ui.theme.GundamPanelLineBright
import com.example.ui.theme.GundamSilver
import com.example.ui.theme.GundamWhite
import com.example.ui.theme.GundamYellow

/**
 * Clean Tactical Mecha Cut Shape for robot armor plates
 */
val MechaArmorCutShape = CutCornerShape(
    topStart = 10.dp,
    topEnd = 0.dp,
    bottomEnd = 10.dp,
    bottomStart = 0.dp
)

val MechaPlateShape = CutCornerShape(
    topStart = 8.dp,
    topEnd = 8.dp,
    bottomEnd = 8.dp,
    bottomStart = 8.dp
)

/**
 * Draws the iconic Gundam V-Fin Crest in Blue, Yellow, and White
 */
@Composable
fun GundamVFinEmblem(
    modifier: Modifier = Modifier,
    finColor: Color = GundamYellow,
    coreColor: Color = GundamBlue,
    opticColor: Color = GundamWhite
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        // Left V-Fin (Iconic Yellow Antenna)
        val leftFin = Path().apply {
            moveTo(cx, cy + h * 0.1f)
            lineTo(cx - w * 0.46f, cy - h * 0.42f)
            lineTo(cx - w * 0.40f, cy - h * 0.48f)
            lineTo(cx - w * 0.08f, cy - h * 0.05f)
            close()
        }
        drawPath(leftFin, color = finColor, style = Fill)

        // Right V-Fin (Iconic Yellow Antenna)
        val rightFin = Path().apply {
            moveTo(cx, cy + h * 0.1f)
            lineTo(cx + w * 0.46f, cy - h * 0.42f)
            lineTo(cx + w * 0.40f, cy - h * 0.48f)
            lineTo(cx + w * 0.08f, cy - h * 0.05f)
            close()
        }
        drawPath(rightFin, color = finColor, style = Fill)

        // Center Diamond Crest (Federation Blue)
        val centerCore = Path().apply {
            moveTo(cx, cy - h * 0.32f)
            lineTo(cx + w * 0.16f, cy)
            lineTo(cx, cy + h * 0.35f)
            lineTo(cx - w * 0.16f, cy)
            close()
        }
        drawPath(centerCore, color = coreColor, style = Fill)
        drawPath(centerCore, color = GundamWhite.copy(alpha = 0.8f), style = Stroke(width = 1.5f))

        // Center Optic Dot (Crisp White)
        drawCircle(
            color = opticColor,
            radius = w * 0.06f,
            center = Offset(cx, cy)
        )
    }
}

/**
 * HUD Tactical Corner Brackets (e.g. ⌜ ⌝ ⌞ ⌟) overlay
 */
@Composable
fun MechaCornerBrackets(
    modifier: Modifier = Modifier,
    bracketColor: Color = GundamBlueLight,
    bracketLength: Dp = 10.dp,
    strokeWidth: Float = 1.8f
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val len = bracketLength.toPx()
        val w = size.width
        val h = size.height

        // Top-Left
        drawLine(bracketColor, Offset(0f, 0f), Offset(len, 0f), strokeWidth)
        drawLine(bracketColor, Offset(0f, 0f), Offset(0f, len), strokeWidth)

        // Top-Right
        drawLine(bracketColor, Offset(w, 0f), Offset(w - len, 0f), strokeWidth)
        drawLine(bracketColor, Offset(w, 0f), Offset(w, len), strokeWidth)

        // Bottom-Left
        drawLine(bracketColor, Offset(0f, h), Offset(len, h), strokeWidth)
        drawLine(bracketColor, Offset(0f, h), Offset(0f, h - len), strokeWidth)

        // Bottom-Right
        drawLine(bracketColor, Offset(w, h), Offset(w - len, h), strokeWidth)
        drawLine(bracketColor, Offset(w, h), Offset(w, h - len), strokeWidth)
    }
}

/**
 * Clean Accent Strip (Yellow/Navy or Blue/Navy)
 */
@Composable
fun MechaHazardBar(
    modifier: Modifier = Modifier,
    stripeColor: Color = GundamYellow,
    baseColor: Color = GundamNavy
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        drawRect(baseColor, size = Size(w, h))

        val step = 16f
        var x = -h
        while (x < w + h) {
            val path = Path().apply {
                moveTo(x, h)
                lineTo(x + step * 0.5f, h)
                lineTo(x + step * 0.5f + h, 0f)
                lineTo(x + h, 0f)
                close()
            }
            drawPath(path, color = stripeColor.copy(alpha = 0.8f))
            x += step
        }
    }
}

/**
 * Mecha HUD Status Tag (e.g. `[SYS.READY]`, `[RX-78]`)
 */
@Composable
fun MechaHudTag(
    text: String,
    modifier: Modifier = Modifier,
    accentColor: Color = GundamYellow,
    backgroundColor: Color = GundamNavy,
    fontSize: Int = 11
) {
    Surface(
        shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.6f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                color = accentColor,
                fontSize = fontSize.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Clean Gundam Cockpit Telemetry Bar (Simple & Uncluttered)
 */
@Composable
fun MechaCockpitHeader(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = GundamNavy,
                shape = CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp)
            )
            .border(
                1.dp,
                GundamPanelLineBright.copy(alpha = 0.6f),
                CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(GundamYellow, CutCornerShape(1.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "GUNDAM SYSTEM // RX-ALARM",
                color = GundamWhite,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 0.5.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "STANDBY",
                color = GundamYellow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

