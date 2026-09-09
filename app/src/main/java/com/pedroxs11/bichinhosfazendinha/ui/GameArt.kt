package com.pedroxs11.bichinhosfazendinha.ui

import androidx.compose.foundation.Image
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
        Text(fallbackEmoji, fontSize = 58.sp)
    }
}
