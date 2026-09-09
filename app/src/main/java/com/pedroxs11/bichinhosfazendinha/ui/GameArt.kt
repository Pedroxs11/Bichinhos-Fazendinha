package com.pedroxs11.bichinhosfazendinha.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pedroxs11.bichinhosfazendinha.R

@Composable
fun AnimalArt(
    animalName: String,
    fallbackEmoji: String,
    modifier: Modifier = Modifier
) {
    if (animalName == "Vaca") {
        Image(
            painter = painterResource(id = R.drawable.cow_face),
            contentDescription = "Vaca",
            modifier = modifier
        )
    } else {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
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
    if (animalName == "Vaca") {
        Image(
            painter = painterResource(id = R.drawable.cow_face),
            contentDescription = "Vaca",
            modifier = modifier
        )
    } else {
        Text(
            text = fallbackEmoji,
            fontSize = if (compact) 38.sp else 88.sp,
            modifier = modifier
        )
    }
}
