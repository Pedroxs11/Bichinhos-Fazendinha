package com.pedroxs11.bichinhosfazendinha.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.pedroxs11.bichinhosfazendinha.R

private fun animalDrawable(animalName: String): Int? = when (animalName) {
    "Pintinho" -> R.drawable.chick_face
    "Coelho" -> R.drawable.rabbit_face
    "Pato" -> R.drawable.duck_face
    "Ovelha" -> R.drawable.sheep_face
    "Cabra" -> R.drawable.goat_face
    "Vaca" -> R.drawable.cow_face
    "Porquinho" -> R.drawable.pig_face
    "Galinha" -> R.drawable.chicken_face
    "Cachorro" -> R.drawable.dog_face
    else -> null
}

@Composable
fun AnimalArt(
    animalName: String,
    fallbackEmoji: String,
    modifier: Modifier = Modifier
) {
    val soundModifier = modifier.clickable { playAnimalSound(animalName) }
    val drawable = animalDrawable(animalName)
    if (drawable != null) {
        Image(
            painter = painterResource(id = drawable),
            contentDescription = "$animalName - toque para ouvir",
            modifier = soundModifier
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
    val drawable = animalDrawable(animalName)
    if (drawable != null) {
        Image(
            painter = painterResource(id = drawable),
            contentDescription = "$animalName - toque para ouvir",
            modifier = soundModifier
        )
    } else {
        Text(
            text = fallbackEmoji,
            fontSize = if (compact) 38.sp else 88.sp,
            modifier = soundModifier
        )
    }
}
