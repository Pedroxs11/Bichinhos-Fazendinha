package com.pedroxs11.bichinhosfazendinha.ui

import androidx.compose.ui.graphics.Color

data class FarmAnimal(
    val id: String,
    val name: String,
    val emoji: String,
    val color: Color,
    val soundLabel: String,
    val unlockCost: Int,
    val startsUnlocked: Boolean = unlockCost == 0
)

val FARM_ANIMALS = listOf(
    FarmAnimal("chick", "Pintinho", "🐥", Color(0xFFFFF4B8), "Piu piu!", 0),
    FarmAnimal("rabbit", "Coelho", "🐰", Color(0xFFFFE6F0), "Fufufu!", 0),
    FarmAnimal("dog", "Cachorro", "🐶", Color(0xFFE8D8C8), "Au au!", ANIMAL_UNLOCK_COSTS.getValue("dog")),
    FarmAnimal("pig", "Porquinho", "🐷", Color(0xFFFFDDE8), "Oinc oinc!", ANIMAL_UNLOCK_COSTS.getValue("pig")),
    FarmAnimal("duck", "Pato", "🦆", Color(0xFFFFF0B5), "Quá quá!", ANIMAL_UNLOCK_COSTS.getValue("duck")),
    FarmAnimal("sheep", "Ovelha", "🐑", Color(0xFFF4F1E8), "Bééé!", ANIMAL_UNLOCK_COSTS.getValue("sheep")),
    FarmAnimal("goat", "Cabra", "🐐", Color(0xFFEDE5D8), "Mééé!", ANIMAL_UNLOCK_COSTS.getValue("goat")),
    FarmAnimal("cow", "Vaca", "🐄", Color(0xFFFFF3D8), "Muuu!", ANIMAL_UNLOCK_COSTS.getValue("cow")),
    FarmAnimal("horse", "Cavalo", "🐴", Color(0xFFEAD5BF), "Ihihih!", ANIMAL_UNLOCK_COSTS.getValue("horse")),
    FarmAnimal("donkey", "Burrinho", "🫏", Color(0xFFE2DED6), "Ióóó!", ANIMAL_UNLOCK_COSTS.getValue("donkey"))
)

fun farmAnimalById(id: String): FarmAnimal? = FARM_ANIMALS.firstOrNull { it.id == id }
fun farmAnimalByName(name: String): FarmAnimal? = FARM_ANIMALS.firstOrNull { it.name == name }
