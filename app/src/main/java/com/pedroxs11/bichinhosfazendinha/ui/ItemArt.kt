package com.pedroxs11.bichinhosfazendinha.ui

import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.pedroxs11.bichinhosfazendinha.R

private fun itemDrawable(title: String): Int? = when (title) {
    "Sons" -> R.drawable.item_sound
    "Cuidar" -> R.drawable.item_care
    "Brincar" -> R.drawable.item_ball
    "Fazendinha" -> R.drawable.item_farm
    "Alimentar" -> R.drawable.item_apple
    "Regar a horta" -> R.drawable.item_watering_can
    "Dar banho" -> R.drawable.item_sponge
    "Secar" -> R.drawable.item_towel
    "Dormir" -> R.drawable.item_sleep
    "Colher frutas" -> R.drawable.item_apple
    "Pegar ovos" -> R.drawable.item_egg
    "Recompensa" -> R.drawable.item_reward
    "Cena horta" -> R.drawable.scene_garden
    "Cena pomar" -> R.drawable.scene_orchard
    "Cena galinheiro" -> R.drawable.scene_coop
    else -> null
}

@Composable
fun ItemArt(
    title: String,
    fallbackEmoji: String,
    modifier: Modifier = Modifier
) {
    val drawable = itemDrawable(title)
    if (drawable != null) {
        Image(
            painter = painterResource(id = drawable),
            contentDescription = title,
            modifier = modifier
        )
    } else {
        Text(fallbackEmoji, fontSize = 58.sp)
    }
}
