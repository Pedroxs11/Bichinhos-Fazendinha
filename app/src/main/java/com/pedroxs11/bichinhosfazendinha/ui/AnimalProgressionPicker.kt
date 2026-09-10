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
fun AnimalProgressionPicker(
    selectedAnimalId: String,
    progression: GameProgression,
    onAnimalSelected: (FarmAnimal) -> Unit,
    onStarsChanged: (Int) -> Unit,
    onMessage: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        FARM_ANIMALS.chunked(2).forEach { rowAnimals ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowAnimals.forEach { animal ->
                    val unlocked = progression.isUnlocked(animal.id, animal.startsUnlocked)
                    val selected = unlocked && animal.id == selectedAnimalId
                    val missing = progression.starsMissingFor(animal.unlockCost)

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(170.dp)
                            .border(
                                width = if (selected) 4.dp else 2.dp,
                                color = if (selected) Color(0xFF4CAF50) else Color.White,
                                shape = RoundedCornerShape(28.dp)
                            )
                            .clickable {
                                if (unlocked) {
                                    onAnimalSelected(animal)
                                    onMessage("${animal.name} escolhido! Vamos brincar?")
                                } else if (missing > 0) {
                                    onMessage("🔒 Faltam $missing ⭐ para liberar ${animal.name}.")
                                } else {
                                    val success = progression.unlock(animal.id, animal.unlockCost)
                                    if (success) {
                                        onStarsChanged(progression.totalStars())
                                        onAnimalSelected(animal)
                                        onMessage("🎉 ${animal.name} foi liberado!")
                                    }
                                }
                            },
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (unlocked) animal.color else Color(0xFFE6E2E6)
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
                                    .background(if (unlocked) Color.White.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.65f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (unlocked) {
                                    AnimalArt(
                                        animalName = animal.name,
                                        fallbackEmoji = animal.emoji,
                                        modifier = Modifier.size(68.dp)
                                    )
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
                                missing > 0 -> Text(
                                    "${animal.unlockCost} ⭐\nFaltam $missing",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF6B5A6D)
                                )
                                else -> Text(
                                    "Toque para liberar • ${animal.unlockCost} ⭐",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF5D3D83)
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
