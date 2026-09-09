package com.pedroxs11.bichinhosfazendinha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class Outfit(
    val id: String,
    val name: String,
    val emoji: String,
    val temporary: Boolean,
    val description: String
)

private val outfits = listOf(
    Outfit("none", "Sem roupa", "🐾", false, "Visual natural"),
    Outfit("party", "Festa", "🎉", false, "Roupa permanente"),
    Outfit("farmer", "Fazendeiro", "👒", false, "Roupa permanente"),
    Outfit("royal", "Realeza", "👑", true, "Especial por 24 horas")
)

@Composable
fun WardrobeApp() {
    var showWardrobe by remember { mutableStateOf(false) }

    MaterialTheme {
        if (showWardrobe) {
            WardrobeScreen(onBack = { showWardrobe = false })
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFBDEBFF))
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    GameApp()
                }

                Button(
                    onClick = { showWardrobe = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .height(58.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E63C7))
                ) {
                    Text("👕 Abrir Armário", fontSize = 19.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun WardrobeScreen(onBack: () -> Unit) {
    var selectedOutfit by remember { mutableStateOf(outfits.first()) }
    var temporaryUnlockedAt by remember { mutableLongStateOf(0L) }
    var message by remember { mutableStateOf("Escolha uma roupa para o bichinho!") }

    val now = System.currentTimeMillis()
    val temporaryDuration = 24L * 60L * 60L * 1000L
    val temporaryUnlocked = temporaryUnlockedAt > 0L && now - temporaryUnlockedAt < temporaryDuration
    val expiresAt = if (temporaryUnlocked) temporaryUnlockedAt + temporaryDuration else 0L
    val remainingHours = if (temporaryUnlocked) {
        ((expiresAt - now) / (60L * 60L * 1000L)).coerceAtLeast(0L) + 1L
    } else 0L

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF6E8))
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(14.dp)) }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Text("←", color = Color(0xFF5C4774), fontSize = 22.sp)
                }

                Text(
                    "Armário 👕",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF5C4774)
                )

                Text("✨", fontSize = 26.sp)
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE8A6))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🐮", fontSize = 86.sp)
                    Text(selectedOutfit.emoji, fontSize = 48.sp)
                    Text(
                        selectedOutfit.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF5D5140)
                    )
                    Text(
                        message,
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        color = Color(0xFF6C6253)
                    )
                }
            }
        }

        item {
            Text(
                "Roupas",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF5C4774)
            )
        }

        items(outfits) { outfit ->
            val available = !outfit.temporary || temporaryUnlocked

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = available) {
                        selectedOutfit = outfit
                        message = if (outfit.temporary) {
                            "Roupa especial ativa por mais ou menos $remainingHours h!"
                        } else {
                            "${outfit.name} vestida!"
                        }
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (available) Color.White else Color(0xFFEDE7F1)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFF7E7FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(outfit.emoji, fontSize = 34.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(outfit.name, fontSize = 19.sp, fontWeight = FontWeight.Black)
                        Text(outfit.description, fontSize = 14.sp, color = Color(0xFF756D79))
                    }

                    if (outfit.temporary && !temporaryUnlocked) {
                        Text("🔒", fontSize = 24.sp)
                    } else {
                        Text("✓", fontSize = 22.sp)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEADCF8))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("👑 Roupa especial do dia", fontSize = 21.sp, fontWeight = FontWeight.Black)

                    if (!temporaryUnlocked) {
                        Text(
                            "Assista a um anúncio premiado para liberar a roupa Realeza por 24 horas.",
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        )
                        Button(
                            onClick = {
                                // Protótipo: simula a conclusão de um anúncio premiado.
                                // Na integração com AdMob, este bloco deve rodar somente após a recompensa confirmada.
                                temporaryUnlockedAt = System.currentTimeMillis()
                                selectedOutfit = outfits.first { it.id == "royal" }
                                message = "Realeza liberada por 24 horas! 👑"
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E63C7))
                        ) {
                            Text("▶ Assistir e liberar 24h", fontSize = 17.sp, fontWeight = FontWeight.Black)
                        }
                    } else {
                        Text(
                            "Liberada! Aproximadamente $remainingHours hora(s) restantes.",
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D3D83)
                        )
                    }
                }
            }
        }

        item {
            Text(
                "Na V1 final, o prazo será salvo no aparelho para continuar contando mesmo se o jogo for fechado.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = Color(0xFF756D79)
            )
        }

        item { Spacer(modifier = Modifier.height(26.dp)) }
    }
}
