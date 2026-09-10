package com.pedroxs11.bichinhosfazendinha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DailyStarProgress(
    dailyStars: Int,
    modifier: Modifier = Modifier
) {
    val safeDaily = dailyStars.coerceIn(0, DAILY_STAR_LIMIT)
    val progress = safeDaily.toFloat() / DAILY_STAR_LIMIT.toFloat()
    val limitReached = safeDaily >= DAILY_STAR_LIMIT
    val milestoneReached = safeDaily in 5 until DAILY_STAR_LIMIT && safeDaily % 5 == 0
    val nextMilestone = if (limitReached) {
        DAILY_STAR_LIMIT
    } else {
        (((safeDaily / 5) + 1) * 5).coerceAtMost(DAILY_STAR_LIMIT)
    }
    val starsToNextMilestone = (nextMilestone - safeDaily).coerceAtLeast(0)
    val starsRemainingToday = (DAILY_STAR_LIMIT - safeDaily).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.94f))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Estrelas de hoje",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF4B5C43)
            )
            Text(
                "⭐ $safeDaily/$DAILY_STAR_LIMIT",
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF6A5B21)
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(10.dp)),
            color = Color(0xFFFFC83D),
            trackColor = Color(0xFFFFF1B8)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(5) { index ->
                val milestone = (index + 1) * 5
                Text(
                    text = if (safeDaily >= milestone) "⭐" else "☆",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = if (safeDaily >= milestone) Color(0xFFFFB300) else Color(0xFFB8B8B8)
                )
            }
        }

        if (!limitReached) {
            Text(
                text = when {
                    safeDaily == 0 -> "🌟 Comece a brincar para ganhar as estrelas de hoje!"
                    starsRemainingToday == 1 -> "⭐ Última estrela do dia!"
                    milestoneReached -> "Marco de $safeDaily estrelas alcançado! ⭐"
                    starsToNextMilestone == 1 -> "Falta 1 ⭐ para o próximo marco"
                    else -> "Faltam $starsToNextMilestone ⭐ para o próximo marco"
                },
                modifier = Modifier.fillMaxWidth(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8A6A00),
                textAlign = TextAlign.Center
            )
        }

        Text(
            text = when {
                limitReached -> "🏆 Meta de hoje completa: $DAILY_STAR_LIMIT/$DAILY_STAR_LIMIT ⭐! Continue brincando; amanhã você ganha mais."
                starsRemainingToday == 1 -> "Ainda dá para ganhar 1 ⭐ hoje."
                else -> "Ainda dá para ganhar $starsRemainingToday ⭐ hoje."
            },
            modifier = Modifier.fillMaxWidth(),
            fontSize = 13.sp,
            fontWeight = if (limitReached) FontWeight.Bold else FontWeight.Medium,
            color = Color(0xFF687064),
            textAlign = TextAlign.Center
        )
    }
}
