package com.pedroxs11.bichinhosfazendinha.ui

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private data class Animal(
    val name: String,
    val emoji: String,
    val color: Color
)

private data class ActivityItem(
    val title: String,
    val emoji: String,
    val subtitle: String,
    val color: Color
)

private val animals = listOf(
    Animal("Vaca", "🐄", Color(0xFFFFF3D8)),
    Animal("Porquinho", "🐷", Color(0xFFFFDDE8)),
    Animal("Galinha", "🐔", Color(0xFFFFE7C2)),
    Animal("Cachorro", "🐶", Color(0xFFE8D8C8))
)

private val activities = listOf(
    ActivityItem("Sons", "🔊", "Escute e descubra os bichinhos", Color(0xFFFFE082)),
    ActivityItem("Cuidar", "🛁", "Comida, banho e carinho", Color(0xFF81D4FA)),
    ActivityItem("Brincar", "⚽", "Diversão com seu bichinho", Color(0xFFFFAB91)),
    ActivityItem("Fazendinha", "🌱", "Plante, colha e cuide da fazenda", Color(0xFFA5D6A7))
)

@Composable
fun GameApp() {
    var selectedAnimal by remember { mutableStateOf(animals.first()) }
    var stars by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("Escolha um bichinho para começar!") }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            FarmBackground()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(14.dp)) }

                item {
                    Header(stars = stars)
                }

                item {
                    MessageBubble(
                        emoji = selectedAnimal.emoji,
                        message = message
                    )
                }

                item {
                    SectionTitle("Escolha seu bichinho")
                }

                items(animals.chunked(2)) { rowAnimals ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowAnimals.forEach { animal ->
                            AnimalCard(
                                modifier = Modifier.weight(1f),
                                animal = animal,
                                selected = animal == selectedAnimal,
                                onClick = {
                                    selectedAnimal = animal
                                    message = "Oi! Eu sou ${animal.name.lowercase()}! Vamos brincar?"
                                }
                            )
                        }
                    }
                }

                item {
                    SectionTitle("O que vamos fazer?")
                }

                items(activities) { activity ->
                    ActivityCard(
                        activity = activity,
                        onPlay = {
                            stars += 1
                            message = activityMessage(activity.title, selectedAnimal)
                        }
                    )
                }

                item { Spacer(modifier = Modifier.height(30.dp)) }
            }
        }
    }
}

@Composable
private fun FarmBackground() {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFBDEBFF))
    ) {
        drawCircle(
            color = Color(0xFFFFE66D),
            radius = 60f,
            center = Offset(size.width - 90f, 90f)
        )
        drawRect(
            color = Color(0xFF9AD66D),
            topLeft = Offset(0f, size.height * 0.55f),
            size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.45f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = 40f,
            center = Offset(70f, 110f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = 30f,
            center = Offset(110f, 110f)
        )
    }
}

@Composable
private fun Header(stars: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Bichinhos",
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF31613A)
            )
            Text(
                text = "& Fazendinha",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4C8B55)
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.95f))
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Text(
                text = "⭐ $stars",
                fontSize = 21.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MessageBubble(emoji: String, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.94f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF5D6)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 38.sp)
            }

            Text(
                text = message,
                modifier = Modifier.weight(1f),
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF3D4F3E)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        fontSize = 22.sp,
        fontWeight = FontWeight.Black,
        color = Color(0xFF315337)
    )
}

@Composable
private fun AnimalCard(
    modifier: Modifier,
    animal: Animal,
    selected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (selected) Color(0xFF4CAF50) else Color.White
    val borderWidth = if (selected) 4.dp else 2.dp

    Card(
        modifier = modifier
            .height(150.dp)
            .border(borderWidth, borderColor, RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = animal.color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = animal.emoji, fontSize = 58.sp)
            Text(
                text = animal.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF40352E)
            )
            if (selected) {
                Text(
                    text = "✓ escolhido",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
private fun ActivityCard(
    activity: ActivityItem,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.96f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(activity.color),
                contentAlignment = Alignment.Center
            ) {
                Text(text = activity.emoji, fontSize = 34.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF344934)
                )
                Text(
                    text = activity.subtitle,
                    fontSize = 14.sp,
                    color = Color(0xFF607060)
                )
            }

            Button(
                onClick = onPlay,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5BAE62)
                )
            ) {
                Text(
                    text = "▶",
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun activityMessage(activity: String, animal: Animal): String = when (activity) {
    "Sons" -> "Que som faz o ${animal.name.lowercase()}? Muito bem! +1 ⭐"
    "Cuidar" -> "${animal.name} ficou limpinho e feliz! +1 ⭐"
    "Brincar" -> "${animal.name} adorou brincar com você! +1 ⭐"
    else -> "A fazendinha ficou ainda mais bonita! +1 ⭐"
}
