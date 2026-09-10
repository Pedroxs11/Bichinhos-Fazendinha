package com.pedroxs11.bichinhosfazendinha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

private val dragActionTitles = setOf(
    "Alimentar",
    "Dar banho",
    "Secar",
    "Regar a horta",
    "Colher frutas"
)

private fun interactionText(title: String, done: Boolean, current: Int, total: Int): String = when {
    done -> "✓ Pronto!"
    title == "Alimentar" -> "Arraste a comida até o bichinho ↑\n${current + 1}/$total"
    title == "Dar banho" -> "Esfregue a esponja ↔\n${current + 1}/$total"
    title == "Secar" -> "Esfregue a toalha ↔\n${current + 1}/$total"
    title in dragActionTitles -> "Arraste o dedo aqui ↔\n${current + 1}/$total"
    else -> "Toque aqui  ${current + 1}/$total"
}

@Composable
fun TapActionPanel(
    emoji: String,
    title: String,
    instruction: String,
    taps: Int,
    requiredTaps: Int,
    onTap: () -> Unit
) {
    val progress = (taps.toFloat() / requiredTaps.toFloat()).coerceIn(0f, 1f)
    val dragEnabled = title in dragActionTitles
    val rubMode = title == "Dar banho" || title == "Secar"
    val feedMode = title == "Alimentar"
    var completionLocked by remember(title) { mutableStateOf(false) }

    fun safeTap() {
        if (completionLocked || taps >= requiredTaps) return
        if (taps + 1 >= requiredTaps) {
            completionLocked = true
        }
        onTap()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.96f))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ItemArt(
            title = title,
            fallbackEmoji = emoji,
            modifier = Modifier.size(70.dp)
        )
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text(instruction, fontSize = 17.sp, textAlign = TextAlign.Center)

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(12.dp)),
            color = if (dragEnabled) Color(0xFF42A5F5) else Color(0xFF5BAE62),
            trackColor = Color(0xFFE7EFE4)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(requiredTaps.coerceAtLeast(1)) { index ->
                Text(
                    text = if (index < taps) "●" else "○",
                    modifier = Modifier.padding(horizontal = 5.dp),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = when {
                        index < taps && dragEnabled -> Color(0xFF42A5F5)
                        index < taps -> Color(0xFF5BAE62)
                        else -> Color(0xFFB9C3B7)
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (dragEnabled) 155.dp else 78.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(if (dragEnabled) Color(0xFFDDF3FF) else Color(0xFF5BAE62))
                .pointerInput(title, taps, requiredTaps, completionLocked) {
                    if (dragEnabled && taps < requiredTaps && !completionLocked) {
                        var accumulatedDistance = 0f
                        var verticalTravel = 0f
                        var actionTriggered = false

                        detectDragGestures(
                            onDragStart = {
                                accumulatedDistance = 0f
                                verticalTravel = 0f
                                actionTriggered = false
                            },
                            onDragEnd = {
                                accumulatedDistance = 0f
                                verticalTravel = 0f
                                actionTriggered = false
                            },
                            onDragCancel = {
                                accumulatedDistance = 0f
                                verticalTravel = 0f
                                actionTriggered = false
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            if (actionTriggered || taps >= requiredTaps || completionLocked) return@detectDragGestures

                            if (feedMode) {
                                verticalTravel += dragAmount.y
                                if (verticalTravel <= -120f) {
                                    actionTriggered = true
                                    safeTap()
                                }
                            } else if (rubMode) {
                                accumulatedDistance += abs(dragAmount.x) + abs(dragAmount.y)
                                if (accumulatedDistance >= 260f) {
                                    actionTriggered = true
                                    safeTap()
                                }
                            } else {
                                accumulatedDistance += abs(dragAmount.x) + abs(dragAmount.y)
                                if (accumulatedDistance >= 140f) {
                                    actionTriggered = true
                                    safeTap()
                                }
                            }
                        }
                    }
                }
                .clickable(
                    enabled = !dragEnabled && taps < requiredTaps && !completionLocked,
                    onClick = { safeTap() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ItemArt(
                    title = title,
                    fallbackEmoji = emoji,
                    modifier = Modifier.size(if (dragEnabled) 60.dp else 40.dp)
                )
                Text(
                    interactionText(title, taps >= requiredTaps, taps, requiredTaps),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = if (dragEnabled) Color(0xFF315B75) else Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        if (dragEnabled && taps < requiredTaps) {
            Text(
                when (title) {
                    "Alimentar" -> "Segure a comida e leve para cima."
                    "Dar banho" -> "Passe o dedo de um lado para o outro como uma esponja."
                    "Secar" -> "Passe o dedo pelo bichinho como uma toalha."
                    else -> "Mova o dedo pela área para completar."
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF607060),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun DragActionPanel(
    emoji: String,
    title: String,
    instruction: String,
    moves: Int,
    requiredMoves: Int,
    onMove: () -> Unit,
    onFallbackTap: () -> Unit
) {
    val progress = (moves.toFloat() / requiredMoves.toFloat()).coerceIn(0f, 1f)
    var completionLocked by remember(title) { mutableStateOf(false) }

    fun safeMove(useFallback: Boolean = false) {
        if (completionLocked || moves >= requiredMoves) return
        if (moves + 1 >= requiredMoves) {
            completionLocked = true
        }
        if (useFallback) onFallbackTap() else onMove()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.96f))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ItemArt(
            title = title,
            fallbackEmoji = emoji,
            modifier = Modifier.size(70.dp)
        )
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text(instruction, fontSize = 17.sp, textAlign = TextAlign.Center)

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(12.dp)),
            color = Color(0xFF42A5F5),
            trackColor = Color(0xFFE5F2FB)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(requiredMoves.coerceAtLeast(1)) { index ->
                Text(
                    text = if (index < moves) "●" else "○",
                    modifier = Modifier.padding(horizontal = 5.dp),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = if (index < moves) Color(0xFF42A5F5) else Color(0xFFB9C3B7)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFFDDF3FF))
                .pointerInput(title, moves, requiredMoves, completionLocked) {
                    if (moves < requiredMoves && !completionLocked) {
                        var accumulatedDistance = 0f
                        var actionTriggered = false
                        detectDragGestures(
                            onDragStart = {
                                accumulatedDistance = 0f
                                actionTriggered = false
                            },
                            onDragEnd = {
                                accumulatedDistance = 0f
                                actionTriggered = false
                            },
                            onDragCancel = {
                                accumulatedDistance = 0f
                                actionTriggered = false
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            if (actionTriggered || moves >= requiredMoves || completionLocked) return@detectDragGestures
                            accumulatedDistance += abs(dragAmount.x) + abs(dragAmount.y)
                            if (accumulatedDistance >= 180f) {
                                actionTriggered = true
                                safeMove()
                            }
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ItemArt(
                    title = title,
                    fallbackEmoji = emoji,
                    modifier = Modifier.size(60.dp)
                )
                Text(
                    if (moves >= requiredMoves) "✓ Pronto!" else "Arraste o dedo aqui ↔",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF315B75),
                    textAlign = TextAlign.Center
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFEAF1E7))
                .clickable(
                    enabled = moves < requiredMoves && !completionLocked,
                    onClick = { safeMove(useFallback = true) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (moves >= requiredMoves) "Concluído!" else "Se precisar, toque aqui",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4F6B4D)
            )
        }
    }
}
