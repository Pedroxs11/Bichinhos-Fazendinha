package com.pedroxs11.bichinhosfazendinha.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.pedroxs11.bichinhosfazendinha.R

private fun animalFallbackDrawable(animalName: String): Int? = when (animalName) {
    "Pintinho" -> R.drawable.chick_face
    "Coelho" -> R.drawable.rabbit_face
    "Pato" -> R.drawable.duck_face
    "Ovelha" -> R.drawable.sheep_face
    "Cabra" -> R.drawable.goat_face
    "Vaca" -> R.drawable.cow_face
    "Porquinho" -> R.drawable.pig_face
    "Galinha" -> R.drawable.chicken_face
    "Cachorro" -> R.drawable.dog_face
    "Cavalo" -> R.drawable.horse_face
    "Burrinho" -> R.drawable.donkey_face
    else -> null
}

private fun animal3dResourceName(animalName: String): String? = when (animalName) {
    "Pintinho" -> "animal_chick_3d"
    "Coelho" -> "animal_rabbit_3d"
    "Cachorro" -> "animal_dog_3d"
    "Porquinho" -> "animal_pig_3d"
    "Pato" -> "animal_duck_3d"
    "Ovelha" -> "animal_sheep_3d"
    "Cabra" -> "animal_goat_3d"
    "Vaca" -> "animal_cow_3d"
    "Cavalo" -> "animal_horse_3d"
    "Burrinho" -> "animal_donkey_3d"
    else -> null
}

@Composable
private fun resolvedAnimalDrawable(animalName: String): Int? {
    val context = LocalContext.current
    val resourceName = animal3dResourceName(animalName)
    if (resourceName != null) {
        val threeD = context.resources.getIdentifier(
            resourceName,
            "drawable",
            context.packageName
        )
        if (threeD != 0) return threeD
    }
    return animalFallbackDrawable(animalName)
}

@Composable
fun AnimalArt(
    animalName: String,
    fallbackEmoji: String,
    modifier: Modifier = Modifier
) {
    val soundModifier = modifier.clickable { playAnimalSound(animalName) }
    val drawable = resolvedAnimalDrawable(animalName)
    if (drawable != null) {
        Image(
            painter = painterResource(id = drawable),
            contentDescription = "$animalName - toque para ouvir",
            modifier = soundModifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Box(modifier = soundModifier, contentAlignment = Alignment.Center) {
            Text(fallbackEmoji, fontSize = 58.sp)
        }
    }
}

@Composable
fun AnimalAvatar(
    animalName: String,
    fallbackEmoji: String,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val soundModifier = modifier.clickable { playAnimalSound(animalName) }
    val drawable = resolvedAnimalDrawable(animalName)
    if (drawable != null) {
        Image(
            painter = painterResource(id = drawable),
            contentDescription = "$animalName - toque para ouvir",
            modifier = soundModifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Text(
            text = fallbackEmoji,
            fontSize = if (compact) 38.sp else 88.sp,
            modifier = soundModifier
        )
    }
}
