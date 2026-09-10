package com.pedroxs11.bichinhosfazendinha.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

private const val PROGRESSION_WARDROBE_PREFS = "wardrobe_prefs"
private const val PROGRESSION_GAME_PREFS = "game_progress"
private const val KEY_PROGRESSION_WARDROBE_ANIMAL = "progression_wardrobe_animal"
private const val KEY_PROGRESSION_ROYAL_AT = "progression_royal_unlocked_at"
private const val PROGRESSION_ROYAL_DURATION_MS = 24L * 60L * 60L * 1000L

private data class ProgressionOutfit(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val temporary: Boolean = false
)

private val progressionOutfits = listOf(
    ProgressionOutfit("none", "Sem roupa", "🐾", "Visual natural"),
    ProgressionOutfit("party", "Festa", "🎉", "Para comemorar"),
    ProgressionOutfit("farmer", "Fazendeiro", "👒", "Pronto para a fazenda"),
    ProgressionOutfit("rain", "Chuvinha", "🌧️", "Dia de chuva"),
    ProgressionOutfit("sport", "Esportista", "⚽", "Hora de brincar"),
    ProgressionOutfit("sleep", "Pijama", "🌙", "Hora de dormir"),
    ProgressionOutfit("summer", "Verão", "☀️", "Dia ensolarado"),
    ProgressionOutfit("royal", "Realeza", "👑", "Especial por 24 horas", temporary = true)
)

private fun progressionOutfitKey(animalId: String): String = "progression_outfit_$animalId"

@Composable
fun ProgressionWardrobeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val wardrobePrefs = remember {
        context.getSharedPreferences(PROGRESSION_WARDROBE_PREFS, Context.MODE_PRIVATE)
    }
    val gamePrefs = remember {
        context.getSharedPreferences(PROGRESSION_GAME_PREFS, Context.MODE_PRIVATE)
    }
    val progression = remember { GameProgression(gamePrefs) }

    val unlockedAnimals = FARM_ANIMALS.filter {
        progression.isUnlocked(it.id, it.startsUnlocked)
    }.ifEmpty { listOf(FARM_ANIMALS.first()) }

    val savedAnimalId = wardrobePrefs.getString(
        KEY_PROGRESSION_WARDROBE_ANIMAL,
        unlockedAnimals.first().id
    ) ?: unlockedAnimals.first().id

    var selectedAnimal by remember {
        mutableStateOf(unlockedAnimals.firstOrNull { it.id == savedAnimalId } ?: unlockedAnimals.first())
    }

    var royalUnlockedAt by remember {
        mutableLongStateOf(wardrobePrefs.getLong(KEY_PROGRESSION_ROYAL_AT, 0L))
    }
    var clockNow by remember { mutableLongStateOf(System.currentTimeMillis()) }

    fun royalValid(now: Long = System.currentTimeMillis()): Boolean {
        val elapsed = now - royalUnlockedAt
        return royalUnlockedAt > 0L && elapsed in 0 until PROGRESSION_ROYAL_DURATION_MS
    }

    fun savedOutfit(animal: FarmAnimal): ProgressionOutfit {
        val id = wardrobePrefs.getString(progressionOutfitKey(animal.id), "none") ?: "none"
        return progressionOutfits.firstOrNull { it.id == id }
            ?.takeIf { !it.temporary || royalValid() }
            ?: progressionOutfits.first()
    }

    fun outfitMessage(animal: FarmAnimal, outfit: ProgressionOutfit, confirmed: Boolean = false): String {
        return if (outfit.id == "none") {
            "${animal.name} está sem roupa${if (confirmed) "! ✅" else "."}"
        } else {
            "${animal.name} está usando ${outfit.name.lowercase()}${if (confirmed) "! ✅" else "."}"
        }
    }

    var selectedOutfit by remember(selectedAnimal.id) {
        mutableStateOf(savedOutfit(selectedAnimal))
    }
    var message by remember {
        mutableStateOf("Escolha uma roupa para ${selectedAnimal.name.lowercase()}!")
    }

    LaunchedEffect(royalUnlockedAt) {
        if (royalUnlockedAt <= 0L) return@LaunchedEffect

        while (true) {
            val now = System.currentTimeMillis()
            clockNow = now
            val remaining = royalUnlockedAt + PROGRESSION_ROYAL_DURATION_MS - now
            val clockMovedBeforeUnlock = now < royalUnlockedAt

            if (remaining <= 0L || clockMovedBeforeUnlock) {
                val editor = wardrobePrefs.edit().remove(KEY_PROGRESSION_ROYAL_AT)
                FARM_ANIMALS.forEach { animal ->
                    if (wardrobePrefs.getString(progressionOutfitKey(animal.id), "none") == "royal") {
                        editor.putString(progressionOutfitKey(animal.id), "none")
                    }
                }
                editor.apply()

                royalUnlockedAt = 0L
                if (selectedOutfit.id == "royal") {
                    selectedOutfit = progressionOutfits.first()
                    message = "A roupa Realeza foi encerrada. Libere novamente quando quiser!"
                }
                break
            }

            delay(minOf(60_000L, remaining))
        }
    }

    val royalAvailable = royalValid(clockNow)
    val remainingMinutes = if (royalAvailable) {
        ((royalUnlockedAt + PROGRESSION_ROYAL_DURATION_MS - clockNow) / 60_000L).coerceAtLeast(0L)
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBack,
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) { Text("←", color = Color(0xFF5C4774), fontSize = 22.sp) }

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
            Text(
                "Bichinhos liberados",
                fontSize = 21.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF5C4774)
            )
        }

        items(unlockedAnimals.chunked(2)) { rowAnimals ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowAnimals.forEach { animal ->
                    val selected = animal.id == selectedAnimal.id
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(120.dp)
                            .border(
                                width = if (selected) 4.dp else 2.dp,
                                color = if (selected) Color(0xFF7A4FA3) else Color.White,
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable {
                                val outfit = savedOutfit(animal)
                                selectedAnimal = animal
                                wardrobePrefs.edit()
                                    .putString(KEY_PROGRESSION_WARDROBE_ANIMAL, animal.id)
                                    .apply()
                                selectedOutfit = outfit
                                message = outfitMessage(animal, outfit)
                            },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected) animal.color else Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            AnimalAvatar(
                                animalName = animal.name,
                                fallbackEmoji = animal.emoji,
                                compact = true,
                                modifier = Modifier.size(62.dp)
                            )
                            Text(animal.name, fontSize = 15.sp, fontWeight = FontWeight.Black)
                            if (selected) Text("✓", color = Color(0xFF5D3D83), fontWeight = FontWeight.Black)
                        }
                    }
                }
                if (rowAnimals.size == 1) Box(modifier = Modifier.weight(1f))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = selectedAnimal.color)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimalAvatar(
                        animalName = selectedAnimal.name,
                        fallbackEmoji = selectedAnimal.emoji,
                        modifier = Modifier.size(116.dp)
                    )
                    Text(selectedOutfit.emoji, fontSize = 46.sp)
                    Text(
                        "${selectedAnimal.name} • ${selectedOutfit.name}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(message, fontSize = 15.sp, textAlign = TextAlign.Center)
                }
            }
        }

        item {
            Text("Roupas", fontSize = 21.sp, fontWeight = FontWeight.Black, color = Color(0xFF5C4774))
        }

        items(progressionOutfits) { outfit ->
            val available = !outfit.temporary || royalAvailable
            val equipped = selectedOutfit.id == outfit.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (equipped) {
                            Modifier.border(
                                width = 3.dp,
                                color = Color(0xFF7A4FA3),
                                shape = RoundedCornerShape(22.dp)
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clickable(enabled = available) {
                        selectedOutfit = outfit
                        wardrobePrefs.edit()
                            .putString(progressionOutfitKey(selectedAnimal.id), outfit.id)
                            .apply()
                        message = outfitMessage(selectedAnimal, outfit, confirmed = true)
                    },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        equipped -> Color(0xFFF3E8FF)
                        available -> Color.White
                        else -> Color(0xFFEDE7F1)
                    }
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFF7E7FF)),
                        contentAlignment = Alignment.Center
                    ) { Text(outfit.emoji, fontSize = 31.sp) }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(outfit.name, fontSize = 18.sp, fontWeight = FontWeight.Black)
                        Text(outfit.description, fontSize = 13.sp, color = Color(0xFF756D79))
                    }

                    Text(
                        when {
                            outfit.temporary && !royalAvailable -> "🔒"
                            equipped -> "✅"
                            else -> "›"
                        },
                        fontSize = 22.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEADCF8))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("👑 Realeza por 24h", fontSize = 20.sp, fontWeight = FontWeight.Black)
                    if (!royalAvailable) {
                        Text(
                            "Área preparada para anúncio premiado. Por enquanto o botão simula a liberação.",
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                val unlockedAt = System.currentTimeMillis()
                                royalUnlockedAt = unlockedAt
                                clockNow = unlockedAt
                                wardrobePrefs.edit()
                                    .putLong(KEY_PROGRESSION_ROYAL_AT, unlockedAt)
                                    .putString(progressionOutfitKey(selectedAnimal.id), "royal")
                                    .apply()
                                selectedOutfit = progressionOutfits.first { it.id == "royal" }
                                message = "Realeza liberada por 24 horas! 👑"
                            },
                            modifier = Modifier.fillMaxWidth().height(58.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8E63C7))
                        ) { Text("▶ Liberar especial", fontWeight = FontWeight.Black) }
                    } else {
                        Text(
                            if (selectedOutfit.id == "royal") {
                                "👑 Vestindo Realeza • restam ${formatProgressionWardrobeTime(remainingMinutes)}."
                            } else {
                                "Realeza liberada • restam ${formatProgressionWardrobeTime(remainingMinutes)}. Toque em Realeza na lista para vestir."
                            },
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D3D83),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        item {
            Text(
                "Quando um novo bichinho for desbloqueado, ele aparece automaticamente aqui e ganha seu próprio armário.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = Color(0xFF756D79)
            )
        }

        item { Spacer(modifier = Modifier.height(26.dp)) }
    }
}

private fun formatProgressionWardrobeTime(minutes: Long): String {
    if (minutes <= 0L) return "menos de 1 min"
    val hours = minutes / 60L
    val remaining = minutes % 60L
    return when {
        hours <= 0L -> "$minutes min"
        remaining == 0L -> "$hours h"
        else -> "$hours h $remaining min"
    }
}
