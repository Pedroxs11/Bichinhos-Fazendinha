package com.pedroxs11.bichinhosfazendinha.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProgressionWardrobeApp() {
    var showWardrobe by remember { mutableStateOf(false) }

    BackHandler(enabled = showWardrobe) {
        showWardrobe = false
    }

    MaterialTheme {
        if (showWardrobe) {
            ProgressionWardrobeScreen(onBack = { showWardrobe = false })
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFBDEBFF))
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    ProgressionGameApp()
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
                    Text(
                        "👕 Abrir Armário",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
