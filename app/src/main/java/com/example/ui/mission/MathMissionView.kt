package com.example.ui.mission

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mission.MathProblem
import com.example.mission.MathProblemGenerator
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

@Composable
fun MathMissionView(
    problemCount: Int,
    difficulty: String,
    onMissionSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val problems = remember { MathProblemGenerator.generate(problemCount, difficulty) }
    var currentProblemIndex by remember { mutableIntStateOf(0) }
    var inputAnswer by remember { mutableStateOf("") }
    var isWrongAnswer by remember { mutableStateOf(false) }

    val currentProblem: MathProblem? = problems.getOrNull(currentProblemIndex)
    val progress = (currentProblemIndex.toFloat() / problems.size.toFloat()).coerceIn(0f, 1f)

    fun onSubmitAnswer() {
        val parsed = inputAnswer.toIntOrNull()
        if (currentProblem != null && parsed == currentProblem.answer) {
            isWrongAnswer = false
            inputAnswer = ""
            if (currentProblemIndex + 1 >= problems.size) {
                onMissionSuccess()
            } else {
                currentProblemIndex++
            }
        } else {
            isWrongAnswer = true
            inputAnswer = ""
        }
    }

    fun onKeyPressed(digit: String) {
        isWrongAnswer = false
        if (inputAnswer.length < 5) {
            inputAnswer += digit
        }
    }

    fun onBackspace() {
        if (inputAnswer.isNotEmpty()) {
            inputAnswer = inputAnswer.dropLast(1)
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
        // Top Progress Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = GundamCardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, GundamBorder),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            tint = GundamBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Soal ${currentProblemIndex + 1} dari ${problems.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GundamBlue
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GundamBlueSubtle,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GundamBlue.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "Tingkat: $difficulty",
                            color = GundamBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = GundamBlue,
                    trackColor = GundamBorder
                )
            }
        }

        // Equation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isWrongAnswer) GundamRed.copy(alpha = 0.08f) else GundamCardBg
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isWrongAnswer) GundamRed.copy(alpha = 0.5f) else GundamBorder
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentProblem?.question ?: "Selesai!",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = if (isWrongAnswer) GundamRed else GundamBlue
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.75f)
                        .height(56.dp)
                        .background(
                            color = GundamBlueSubtle,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, GundamBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (inputAnswer.isEmpty()) "?" else inputAnswer,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = if (inputAnswer.isEmpty()) GundamTextSecondary else GundamBlue
                    )
                }

                AnimatedVisibility(visible = isWrongAnswer) {
                    Text(
                        text = "Jawaban salah! Coba hitung lagi.",
                        color = GundamRed,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        // Keypad (1..9, 0, Backspace, Submit)
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val keyRows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("back", "0", "ok")
            )

            for (row in keyRows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (key in row) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("keypad_$key"),
                            shape = RoundedCornerShape(14.dp),
                            color = when (key) {
                                "ok" -> GundamBlue
                                "back" -> GundamBlueSubtle
                                else -> GundamCardBg
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when (key) {
                                    "ok" -> GundamBlue
                                    "back" -> GundamBlue.copy(alpha = 0.3f)
                                    else -> GundamBorder
                                }
                            ),
                            onClick = {
                                when (key) {
                                    "back" -> onBackspace()
                                    "ok" -> onSubmitAnswer()
                                    else -> onKeyPressed(key)
                                }
                            }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                when (key) {
                                    "back" -> Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                        contentDescription = "Hapus",
                                        tint = GundamBlue
                                    )
                                    "ok" -> Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Kirim",
                                        tint = GundamWhite,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    else -> Text(
                                        text = key,
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GundamTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
