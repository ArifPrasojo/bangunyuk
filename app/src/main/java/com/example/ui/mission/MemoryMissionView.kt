package com.example.ui.mission

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GundamBlue
import com.example.ui.theme.GundamBlueSubtle
import com.example.ui.theme.GundamBorder
import com.example.ui.theme.GundamCanvasBg
import com.example.ui.theme.GundamCardBg
import com.example.ui.theme.GundamGreen
import com.example.ui.theme.GundamRed
import com.example.ui.theme.GundamTextPrimary
import com.example.ui.theme.GundamTextSecondary
import com.example.ui.theme.GundamWhite
import com.example.ui.theme.GundamYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun MemoryMissionView(
    sequenceLength: Int = 4,
    onMissionSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val pattern = remember { List(sequenceLength) { Random.nextInt(0, 4) } }
    val userInputs = remember { mutableStateListOf<Int>() }

    var highlightedTile by remember { mutableIntStateOf(-1) }
    var isShowingPattern by remember { mutableStateOf(true) }
    var isWrongSequence by remember { mutableStateOf(false) }

    // Gundam Palette tiles: Blue, Yellow, Red, Green
    val tileColors = listOf(
        GundamBlue,
        GundamYellow,
        GundamRed,
        GundamGreen
    )

    fun playPattern() {
        scope.launch {
            isShowingPattern = true
            userInputs.clear()
            isWrongSequence = false
            delay(600)
            for (tile in pattern) {
                highlightedTile = tile
                delay(500)
                highlightedTile = -1
                delay(250)
            }
            isShowingPattern = false
        }
    }

    LaunchedEffect(Unit) {
        playPattern()
    }

    fun onTileClick(index: Int) {
        if (isShowingPattern) return

        userInputs.add(index)
        val currentIndex = userInputs.size - 1

        if (pattern[currentIndex] != index) {
            // Wrong
            isWrongSequence = true
            playPattern()
        } else {
            if (userInputs.size == pattern.size) {
                // Done!
                onMissionSuccess()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GundamCanvasBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GundamCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, GundamBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = null,
                        tint = GundamBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Misi Pola Ingatan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GundamBlue
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isShowingPattern) "Perhatikan urutan pola menyala..." else "Ulangi urutan pola tadi!",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isShowingPattern) GundamBlue else GundamTextSecondary,
                    fontWeight = if (isShowingPattern) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 2x2 Grid Tiles
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GundamCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, GundamBorder),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .aspectRatio(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MemoryTile(0, tileColors[0], highlightedTile == 0, isShowingPattern) { onTileClick(0) }
                    MemoryTile(1, tileColors[1], highlightedTile == 1, isShowingPattern) { onTileClick(1) }
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MemoryTile(2, tileColors[2], highlightedTile == 2, isShowingPattern) { onTileClick(2) }
                    MemoryTile(3, tileColors[3], highlightedTile == 3, isShowingPattern) { onTileClick(3) }
                }
            }
        }

        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isWrongSequence) GundamRed.copy(alpha = 0.08f) else GundamBlueSubtle
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isWrongSequence) GundamRed.copy(alpha = 0.5f) else GundamBlue.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = if (isWrongSequence) "Pola salah! Mengulang demonstrasi..." else "Kemajuan: ${userInputs.size} / ${pattern.size} langkah",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = if (isWrongSequence) GundamRed else GundamBlue
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.MemoryTile(
    index: Int,
    baseColor: Color,
    isHighlighted: Boolean,
    isShowingPattern: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .weight(1f)
            .fillMaxSize()
            .testTag("memory_tile_$index"),
        shape = RoundedCornerShape(16.dp),
        color = if (isHighlighted) baseColor else baseColor.copy(alpha = 0.25f),
        border = androidx.compose.foundation.BorderStroke(
            if (isHighlighted) 2.dp else 1.dp,
            if (isHighlighted) baseColor else baseColor.copy(alpha = 0.5f)
        ),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "${index + 1}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = if (isHighlighted) GundamWhite else baseColor
            )
        }
    }
}
