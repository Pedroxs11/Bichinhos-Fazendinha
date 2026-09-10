package com.pedroxs11.bichinhosfazendinha.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AnimalProgressionPicker(
    selectedAnimalId: String,
    progression: GameProgression,
    onAnimalSelected: (FarmAnimal) -> Unit,
    onStarsChanged: (Int) -> Unit,
    onMessage: (String) -> Unit
) {
    var celebrationAnimal by remember { mutableStateOf<FarmAnimal?>(null) }

    val nextAnimal = FARM_ANIMALS.firstOrNull { animal ->
        if (progression.isUnlocked(animal.id, animal.startsUnlocked)) return@firstOrNull false
        val index = FARM_ANIMALS.indexOfFirst { it.id == animal.id }
        val previous = FARM_ANIMALS.getOrNull(index - 1)
        previous == null || progression.isUnlocked(previous.id, previous.startsUnlocked)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        celebrationAnimal?.let { animal ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { celebrationAnimal = null },
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE98A))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🎉✨ NOVO BICHINHO! ✨🎉", fontSize = 21.sp, fontWeight = FontWeight.Black)
                    AnimalArt(
                        animalName = animal.name,
                        fallbackEmoji = animal.emoji,
                        modifier = Modifier.size(96.dp)
                    )
                    Text(
                        animal.name,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF4F461F)
                    )
                    Text(
                        "Agora ele faz parte da sua fazendinha!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF6A6045)
                    )
                    Text(
                        "Toque aqui para continuar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF78651C)
                    )
                }
            }
        }

        if (nextAnimal != null) {
            val currentStars = progression.totalStars()
            val missing = progression.starsMissingFor(nextAnimal.unlockCost)
            val unlockProgress = if (nextAnimal.unlockCost <= 0) {
                1f
            } else {
                (currentStars.toFloat() / nextAnimal.unlockCost.toFloat()).coerceIn(0f, 1f)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4B8))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(58.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = 0.75f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(nextAnimal.emoji, fontSize = 35.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Próximo bichinho",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78651C)
                            )
                            Text(
                                nextAnimal.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4F461F)
                            )
                        }
                        Text(
                            "${nextAnimal.unlockCost} ⭐",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF6A5B21)
                        )
                    }

                    LinearProgressIndicator(
                        progress = { unlockProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        color = Color(0xFFFFC83D),
                        trackColor = Color.White.copy(alpha = 0.8f)
                    )

                    Text(
                        text = if (missing > 0) {
                            "Você tem $currentStars ⭐ • faltam $missing ⭐"
                        } else {
                            "🎉 Já dá para liberar ${nextAnimal.name}! Toque nele abaixo."
                        },
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (missing > 0) Color(0xFF6A6045) else Color(0xFF2E7D32)
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDDF3D5))
            ) {
                Text(
                    "🏆 Todos os bichinhos foram liberados!",
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF315337)
                )
            }
        }

        FARM_ANIMALS.chunked(2).forEach { rowAnimals ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowAnimals.forEach { animal ->
                    val unlocked = progression.isUnlocked(animal.id, animal.startsUnlocked)
                    val selected = unlocked && animal.id == selectedAnimalId
                    val missing = progression.starsMissingFor(animal.unlockCost)
                    val animalIndex = FARM_ANIMALS.indexOfFirst { it.id == animal.id }
                    val previousAnimal = FARM_ANIMALS.getOrNull(animalIndex - 1)
                    val previousUnlocked = previousAnimal == null ||
                        progression.isUnlocked(previousAnimal.id, previousAnimal.startsUnlocked)
                    val isNextTarget = nextAnimal?.id == animal.id

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(174.dp)
                            .border(
                                width = when {
                                    selected -> 4.dp
                                    isNextTarget -> 3.dp
                                    else -> 2.dp
                                },
                                color = when {
                                    selected -> Color(0xFF4CAF50)
                                    isNextTarget -> Color(0xFFFFB300)
                                    else -> Color.White
                                },
                                shape = RoundedCornerShape(28.dp)
                            )
                            .clickable {
                                when {
                                    unlocked -> {
                                        onAnimalSelected(animal)
                                        onMessage("${animal.name} escolhido! Vamos brincar?")
                                    }
                                    !previousUnlocked && previousAnimal != null -> {
                                        onMessage("🔒 Primeiro libere ${previousAnimal.name} para chegar em ${animal.name}.")
                                    }
                                    missing > 0 -> {
                                        onMessage("🔒 Faltam $missing ⭐ para liberar ${animal.name}.")
                                    }
                                    else -> {
                                        val success = progression.unlock(animal.id, animal.unlockCost)
                                        if (success) {
                                            celebrationAnimal = animal
                                            onStarsChanged(progression.totalStars())
                                            onAnimalSelected(animal)
                                            onMessage("🎉✨ NOVO BICHINHO! ${animal.name} foi liberado! ✨🎉")
                                        }
                                    }
                                }
                            },
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                unlocked -> animal.color
                                isNextTarget -> Color(0xFFFFEFB0)
                                else -> Color(0xFFE6E2E6)
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(24.dp))
                                    .background(
                                        if (unlocked) Color.White.copy(alpha = 0.45f)
                                        else Color.White.copy(alpha = 0.65f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (unlocked) {
                                    AnimalArt(
                                        animalName = animal.name,
                                        fallbackEmoji = animal.emoji,
                                        modifier = Modifier.size(68.dp)
                                    )
                                } else if (isNextTarget) {
                                    Text("${animal.emoji} 🔒", fontSize = 30.sp)
                                } else {
                                    Text("🔒", fontSize = 38.sp)
                                }
                            }

                            Text(
                                animal.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center
                            )

                            when {
                                unlocked && selected -> Text(
                                    "✓ escolhido",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                                unlocked -> Text(
                                    "Liberado",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                                !previousUnlocked && previousAnimal != null -> Text(
                                    "Libere ${previousAnimal.name} primeiro",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF6B5A6D)
                                )
                                missing > 0 -> Text(
                                    "${animal.unlockCost} ⭐\nFaltam $missing",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF6B5A6D)
                                )
                                else -> Text(
                                    "🎉 Pode liberar!\n${animal.unlockCost} ⭐",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }

                if (rowAnimals.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
