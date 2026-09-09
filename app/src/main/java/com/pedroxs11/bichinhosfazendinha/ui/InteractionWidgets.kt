package com.pedroxs11.bichinhosfazendinha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.96f))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(emoji, fontSize = 66.sp)
        Text(title, fontSize = 24.sp, fontWeight = FontWeight.Black)
        Text(instruction, fontSize = 17.sp, textAlign = TextAlign.Center)

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(12.dp)),
            color = Color(0xFF5BAE62),
            trackColor = Color(0xFFE7EFE4)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(78.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF5BAE62))
                .clickable(onClick = onTap),
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (taps >= requiredTaps) "✓ Pronto!" else "$emoji Toque aqui  ${taps + 1}/$requiredTaps",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White.copy(alpha = 0.96f))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(emoji, fontSize = 66.sp)
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

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(Color(0xFFDDF3FF))
                .pointerInput(moves, requiredMoves) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        if (moves < requiredMoves && (dragAmount.x != 0f || dragAmount.y != 0f)) {
                            onMove()
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (moves >= requiredMoves) "✓ Pronto!" else "$emoji  Arraste o dedo aqui ↔",
                fontSize = 21.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF315B75),
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFEAF1E7))
                .clickable(enabled = moves < requiredMoves, onClick = onFallbackTap),
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
