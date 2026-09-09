package com.pedroxs11.bichinhosfazendinha.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class Animal(val name: String, val emoji: String)
private data class ActivityItem(val title: String, val emoji: String)

private val animals = listOf(
    Animal("Vaca", "🐄"),
    Animal("Porquinho", "🐷"),
    Animal("Galinha", "🐔"),
    Animal("Cachorro", "🐶")
)

private val activities = listOf(
    ActivityItem("Sons", "🔊"),
    ActivityItem("Cuidar", "🛁"),
    ActivityItem("Brincar", "⚽"),
    ActivityItem("Fazendinha", "🌱")
)

@Composable
fun GameApp() {
    var selectedAnimal by remember { mutableStateOf(animals.first()) }
    var stars by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("Escolha um bichinho e uma atividade!") }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Bichinhos & Fazendinha",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "⭐ $stars")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = message)
                }

                item {
                    Text(
                        text = "Escolha seu bichinho",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                items(animals.chunked(2)) { rowAnimals ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowAnimals.forEach { animal ->
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    selectedAnimal = animal
                                    message = "${animal.emoji} ${animal.name} escolhido!"
                                }
                            ) {
                                Text("${animal.emoji} ${animal.name}")
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "O que vamos fazer com ${selectedAnimal.name}?",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                items(activities) { activity ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${activity.emoji} ${activity.title}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Button(onClick = {
                                stars += 1
                                message = activityMessage(activity.title, selectedAnimal)
                            }) {
                                Text("Jogar")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun activityMessage(activity: String, animal: Animal): String = when (activity) {
    "Sons" -> "${animal.emoji} Que som faz o ${animal.name.lowercase()}? +1 ⭐"
    "Cuidar" -> "🛁 Você cuidou do ${animal.name.lowercase()}! +1 ⭐"
    "Brincar" -> "⚽ O ${animal.name.lowercase()} adorou brincar! +1 ⭐"
    else -> "🌱 Trabalho concluído na fazendinha! +1 ⭐"
}
