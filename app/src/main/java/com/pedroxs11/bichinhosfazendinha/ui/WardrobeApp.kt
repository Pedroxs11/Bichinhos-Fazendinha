package com.pedroxs11.bichinhosfazendinha.ui

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val WARDROBE_PREFS = "wardrobe_prefs"
private const val KEY_ROYAL_UNLOCKED_AT = "royal_unlocked_at"
private const val LEGACY_KEY_SELECTED_OUTFIT = "selected_outfit"
private const val KEY_SELECTED_ANIMAL = "selected_wardrobe_animal"
private const val TEMPORARY_DURATION_MS = 24L * 60L * 60L * 1000L

private data class Outfit(
    val id: String,
    val name: String,
    val emoji: String,
    val temporary: Boolean,
    val description: String
)

private data class WardrobeAnimal(
    val id: String,
    val name: String,
    val emoji: String,
    val color: Color
)

private val outfits = listOf(
    Outfit("none", "Sem roupa", "🐾", false, "Visual natural"),
    Outfit("party", "Festa", "🎉", false, "Roupa permanente"),
    Outfit("farmer", "Fazendeiro", "👒", false, "Roupa permanente"),
    Outfit("royal", "Realeza", "👑", true, "Especial por 24 horas")
)

private val wardrobeAnimals = listOf(
    WardrobeAnimal("cow", "Vaca", "🐮", Color(0xFFFFE8A6)),
    WardrobeAnimal("pig", "Porquinho", "🐷", Color(0xFFFFDDE8)),
    WardrobeAnimal("chicken", "Galinha", "🐔", Color(0xFFFFE7C2)),
    WardrobeAnimal("dog", "Cachorro", "🐶", Color(0xFFE8D8C8))
)

private fun outfitKey(animalId: String): String = "selected_outfit_$animalId"

@Composable
fun WardrobeApp() {
    var showWardrobe by remember { mutableStateOf(false) }

    BackHandler(enabled = showWardrobe) {
        showWardrobe = false
    }

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
    val context = LocalContext.current
    val prefs = remember {
        context.getSharedPreferences(WARDROBE_PREFS, Context.MODE_PRIVATE)
    }

    var temporaryUnlockedAt by remember {
        mutableLongStateOf(prefs.getLong(KEY_ROYAL_UNLOCKED_AT, 0L))
    }

    val savedAnimalId = prefs.getString(KEY_SELECTED_ANIMAL, wardrobeAnimals.first().id)
        ?: wardrobeAnimals.first().id
    var selectedAnimal by remember {
        mutableStateOf(wardrobeAnimals.firstOrNull { it.id == savedAnimalId } ?: wardrobeAnimals.first())
    }

    fun savedOutfitFor(animal: WardrobeAnimal, temporaryValid: Boolean): Outfit {
        val legacyCowOutfit = if (animal.id == "cow") {
            prefs.getString(LEGACY_KEY_SELECTED_OUTFIT, null)
        } else null
        val savedId = prefs.getString(outfitKey(animal.id), legacyCowOutfit ?: "none") ?: "none"
        return outfits.firstOrNull { it.id == savedId }
            ?.takeIf { !it.temporary || temporaryValid }
            ?: outfits.first()
    }

    val nowAtOpen = System.currentTimeMillis()
    val royalValidAtOpen = temporaryUnlockedAt > 0L &&
        nowAtOpen - temporaryUnlockedAt < TEMPORARY_DURATION_MS

    var selectedOutfit by remember {
        mutableStateOf(savedOutfitFor(selectedAnimal, royalValidAtOpen))
    }
    var message by remember {
        mutableStateOf("${selectedOutfit.name} em ${selectedAnimal.name.lowercase()}. Escolha outro visual quando quiser!")
    }

    val now = System.currentTimeMillis()
    val temporaryUnlocked = temporaryUnlockedAt > 0L && now - temporaryUnlockedAt < TEMPORARY_DURATION_MS
    val expiresAt = if (temporaryUnlocked) temporaryUnlockedAt + TEMPORARY_DURATION_MS else 0L
    val remainingMinutes = if (temporaryUnlocked) {
        ((expiresAt - now) / (60L * 1000L)).coerceAtLeast(0L)
    } else 0L
    val remainingHours = if (temporaryUnlocked) {
        (remainingMinutes / 60L).coerceAtLeast(0L)
    } else 0L

    if (!temporaryUnlocked && temporaryUnlockedAt > 0L) {
        prefs.edit().remove(KEY_ROYAL_UNLOCKED_AT).apply()
        temporaryUnlockedAt = 0L
        wardrobeAnimals.forEach { animal ->
            if (prefs.getString(outfitKey(animal.id), "none") == "royal") {
                prefs.edit().putString(outfitKey(animal.id), "none").apply()
            }
        }
        if (selectedOutfit.id == "royal") {
            selectedOutfit = outfits.first()
            message = "A roupa Realeza expirou. Assista novamente para liberar por mais 24h."
        }
    }

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
            Text(
                "Escolha o bichinho",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF5C4774)
            )
        }

        items(wardrobeAnimals.chunked(2)) { rowAnimals ->
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
                            .clickable {
                                selectedAnimal = animal
                                prefs.edit().putString(KEY_SELECTED_ANIMAL, animal.id).apply()
                                selectedOutfit = savedOutfitFor(animal, temporaryUnlocked)
                                message = "${selectedOutfit.name} em ${animal.name.lowercase()}."
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
                                modifier = Modifier.size(64.dp)
                            )
                            Text(animal.name, fontSize = 16.sp, fontWeight = FontWeight.Black)
                            if (selected) Text("✓", fontWeight = FontWeight.Black, color = Color(0xFF5D3D83))
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = selectedAnimal.color)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimalAvatar(
                        animalName = selectedAnimal.name,
                        fallbackEmoji = selectedAnimal.emoji,
                        modifier = Modifier.size(118.dp)
                    )
                    Text(selectedOutfit.emoji, fontSize = 48.sp)
                    Text(
                        "${selectedAnimal.name} • ${selectedOutfit.name}",
                        fontSize = 23.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF5D5140),
                        textAlign = TextAlign.Center
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
            val equipped = selectedOutfit.id == outfit.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = available) {
                        selectedOutfit = outfit
                        prefs.edit()
                            .putString(outfitKey(selectedAnimal.id), outfit.id)
                            .apply()
                        message = if (outfit.temporary) {
                            "${selectedAnimal.name} está de Realeza! Restam ${formatRemainingTime(remainingHours, remainingMinutes)}."
                        } else {
                            "${outfit.name} salva para ${selectedAnimal.name.lowercase()}!"
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

                    when {
                        outfit.temporary && !temporaryUnlocked -> Text("🔒", fontSize = 24.sp)
                        equipped -> Text("✅", fontSize = 22.sp)
                        else -> Text("✓", fontSize = 22.sp)
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
                                val unlockedAt = System.currentTimeMillis()
                                prefs.edit()
                                    .putLong(KEY_ROYAL_UNLOCKED_AT, unlockedAt)
                                    .putString(outfitKey(selectedAnimal.id), "royal")
                                    .apply()
                                temporaryUnlockedAt = unlockedAt
                                selectedOutfit = outfits.first { it.id == "royal" }
                                message = "Realeza liberada por 24 horas para ${selectedAnimal.name.lowercase()}! 👑"
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
                            "Liberada! Restam ${formatRemainingTime(remainingHours, remainingMinutes)}.",
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
                "Cada bichinho guarda sua própria roupa. As escolhas e o prazo de 24 horas ficam salvos no aparelho.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                color = Color(0xFF756D79)
            )
        }

        item { Spacer(modifier = Modifier.height(26.dp)) }
    }
}

private fun formatRemainingTime(hours: Long, minutes: Long): String {
    return when {
        hours >= 1L -> "$hours h ${minutes % 60L} min"
        minutes >= 1L -> "$minutes min"
        else -> "menos de 1 min"
    }
}
