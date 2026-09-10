package com.pedroxs11.bichinhosfazendinha.ui

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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

private const val V2_PREFS = "game_progress"
private const val KEY_V2_SELECTED_ANIMAL = "selected_progression_animal"

private enum class ProgressionScreen { HOME, CARE, SOUNDS, FARM, PLAY }

private data class ProgressionStep(
    val title: String,
    val emoji: String,
    val instruction: String,
    val success: String
)

private val progressionCareSteps = listOf(
    ProgressionStep("Alimentar", "🍎", "Arraste a comida até o bichinho.", "Barriguinha cheia!"),
    ProgressionStep("Dar banho", "🛁", "Esfregue a esponja para limpar.", "Agora está limpinho!"),
    ProgressionStep("Secar", "🧻", "Passe a toalha pelo bichinho.", "Prontinho e sequinho!"),
    ProgressionStep("Dormir", "🌙", "Toque para colocar o bichinho para dormir.", "Boa noite! Zzz...")
)

private val progressionFarmSteps = listOf(
    ProgressionStep("Regar a horta", "💧", "Arraste para regar as plantinhas.", "A horta ficou verdinha!"),
    ProgressionStep("Colher frutas", "🍎", "Arraste para colher as frutas.", "Cestinha cheia!"),
    ProgressionStep("Pegar ovos", "🥚", "Toque para recolher os ovos.", "Ovos guardados!")
)

@Composable
fun ProgressionGameApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(V2_PREFS, Context.MODE_PRIVATE) }
    val progression = remember { GameProgression(prefs) }

    val savedId = prefs.getString(KEY_V2_SELECTED_ANIMAL, FARM_ANIMALS.first().id)
        ?: FARM_ANIMALS.first().id
    val initialAnimal = FARM_ANIMALS.firstOrNull {
        it.id == savedId && progression.isUnlocked(it.id, it.startsUnlocked)
    } ?: FARM_ANIMALS.first()

    var selectedAnimal by remember { mutableStateOf(initialAnimal) }
    var stars by remember { mutableIntStateOf(progression.totalStars()) }
    var dailyStars by remember { mutableIntStateOf(progression.dailyStars()) }
    var message by remember { mutableStateOf("Escolha um bichinho e vamos brincar!") }
    var screen by remember { mutableStateOf(ProgressionScreen.HOME) }

    BackHandler(enabled = screen != ProgressionScreen.HOME) {
        screen = ProgressionScreen.HOME
    }

    fun reward(amount: Int, successText: String) {
        val result = progression.rewardStars(amount)
        stars = result.totalStars
        dailyStars = result.dailyStars
        message = when {
            result.granted == amount -> "$successText +${result.granted} ⭐"
            result.granted > 0 -> "$successText +${result.granted} ⭐ • limite diário atingido"
            else -> "$successText Você já ganhou as $DAILY_STAR_LIMIT ⭐ de hoje. Volte amanhã para ganhar mais!"
        }
        screen = ProgressionScreen.HOME
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFBDEBFF))
        ) {
            when (screen) {
                ProgressionScreen.HOME -> ProgressionHome(
                    selectedAnimal = selectedAnimal,
                    stars = stars,
                    dailyStars = dailyStars,
                    message = message,
                    progression = progression,
                    onAnimalSelected = {
                        selectedAnimal = it
                        prefs.edit().putString(KEY_V2_SELECTED_ANIMAL, it.id).apply()
                        message = "${it.name} escolhido! Vamos brincar?"
                    },
                    onStarsChanged = {
                        stars = it
                        dailyStars = progression.dailyStars()
                    },
                    onMessage = { message = it },
                    onOpen = { screen = it }
                )

                ProgressionScreen.CARE -> ProgressionCareScreen(
                    animal = selectedAnimal,
                    stars = stars,
                    onBack = { screen = ProgressionScreen.HOME },
                    onDone = { reward(5, "${selectedAnimal.name} está feliz, limpinho e descansado!") }
                )

                ProgressionScreen.SOUNDS -> ProgressionSoundsScreen(
                    progression = progression,
                    stars = stars,
                    onBack = { screen = ProgressionScreen.HOME },
                    onDone = { reward(3, "Você descobriu os sons dos bichinhos!") }
                )

                ProgressionScreen.FARM -> ProgressionFarmScreen(
                    stars = stars,
                    onBack = { screen = ProgressionScreen.HOME },
                    onDone = { reward(4, "A fazendinha está cuidada e cheia de vida!") }
                )

                ProgressionScreen.PLAY -> ProgressionPlayScreen(
                    animal = selectedAnimal,
                    stars = stars,
                    onBack = { screen = ProgressionScreen.HOME },
                    onDone = { reward(2, "${selectedAnimal.name} adorou brincar com você!") }
                )
            }
        }
    }
}

@Composable
private fun ProgressionHome(
    selectedAnimal: FarmAnimal,
    stars: Int,
    dailyStars: Int,
    message: String,
    progression: GameProgression,
    onAnimalSelected: (FarmAnimal) -> Unit,
    onStarsChanged: (Int) -> Unit,
    onMessage: (String) -> Unit,
    onOpen: (ProgressionScreen) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(14.dp)) }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Bichinhos", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color(0xFF31613A))
                    Text("& Fazendinha", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4C8B55))
                }
                Text("⭐ $stars", fontSize = 22.sp, fontWeight = FontWeight.Black)
            }
        }
        item { DailyStarProgress(dailyStars = dailyStars) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AnimalAvatar(
                        animalName = selectedAnimal.name,
                        fallbackEmoji = selectedAnimal.emoji,
                        compact = true,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(message, modifier = Modifier.weight(1f), fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            Text("Escolha seu bichinho", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF315337))
        }
        item {
            AnimalProgressionPicker(
                selectedAnimalId = selectedAnimal.id,
                progression = progression,
                onAnimalSelected = onAnimalSelected,
                onStarsChanged = onStarsChanged,
                onMessage = onMessage
            )
        }
        item { Text("O que vamos fazer?", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color(0xFF315337)) }
        item {
            ProgressionActivityButton("🔊", "Sons", "Escute e descubra os bichinhos") { onOpen(ProgressionScreen.SOUNDS) }
        }
        item {
            ProgressionActivityButton("🛁", "Cuidar", "Comida, banho, secar e dormir") { onOpen(ProgressionScreen.CARE) }
        }
        item {
            ProgressionActivityButton("⚽", "Brincar", "Jogue bola com seu bichinho") { onOpen(ProgressionScreen.PLAY) }
        }
        item {
            ProgressionActivityButton("🌱", "Fazendinha", "Regue, colha e pegue ovos") { onOpen(ProgressionScreen.FARM) }
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun ProgressionActivityButton(emoji: String, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(emoji, fontSize = 34.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(subtitle, fontSize = 14.sp, color = Color(0xFF607060))
            }
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5BAE62))
            ) { Text("▶") }
        }
    }
}

@Composable
private fun ProgressionCareScreen(animal: FarmAnimal, stars: Int, onBack: () -> Unit, onDone: () -> Unit) {
    ProgressionStepsScreen(
        title = "Cuidar",
        animal = animal,
        stars = stars,
        steps = progressionCareSteps,
        requiredActions = 3,
        onBack = onBack,
        rewardText = "Receber até 5 estrelas",
        onDone = onDone
    )
}

@Composable
private fun ProgressionFarmScreen(stars: Int, onBack: () -> Unit, onDone: () -> Unit) {
    ProgressionStepsScreen(
        title = "Fazendinha",
        animal = null,
        stars = stars,
        steps = progressionFarmSteps,
        requiredActions = 4,
        onBack = onBack,
        rewardText = "Receber até 4 estrelas",
        onDone = onDone
    )
}

@Composable
private fun ProgressionStepsScreen(
    title: String,
    animal: FarmAnimal?,
    stars: Int,
    steps: List<ProgressionStep>,
    requiredActions: Int,
    onBack: () -> Unit,
    rewardText: String,
    onDone: () -> Unit
) {
    var currentStep by remember(animal?.id, title) { mutableIntStateOf(0) }
    var actions by remember(animal?.id, title, currentStep) { mutableIntStateOf(0) }
    var feedback by remember(animal?.id, title) { mutableStateOf("Vamos começar!") }
    val step = steps.getOrNull(currentStep)
    val finished = step == null
    val progress = if (finished) 1f else currentStep.toFloat() / steps.size.toFloat()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(14.dp)) }
        item { ProgressionTopBar(title, stars, onBack) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = animal?.color ?: Color(0xFFDDF3D5))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (animal != null) {
                        AnimalAvatar(animal.name, animal.emoji, modifier = Modifier.size(112.dp))
                        Text(animal.name, fontSize = 25.sp, fontWeight = FontWeight.Black)
                    } else {
                        Text("🌾", fontSize = 68.sp)
                    }
                    Text(feedback, textAlign = TextAlign.Center, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        item {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(12.dp)),
                color = Color(0xFF5BAE62),
                trackColor = Color.White
            )
        }
        if (step != null) {
            item {
                TapActionPanel(
                    emoji = step.emoji,
                    title = step.title,
                    instruction = step.instruction,
                    taps = actions,
                    requiredTaps = requiredActions,
                    onTap = {
                        val next = (actions + 1).coerceAtMost(requiredActions)
                        actions = next
                        if (next >= requiredActions) {
                            feedback = step.success
                            currentStep = (currentStep + 1).coerceAtMost(steps.size)
                        }
                    }
                )
            }
        } else {
            item {
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5BAE62))
                ) { Text(rewardText, fontSize = 18.sp, fontWeight = FontWeight.Black) }
            }
        }
        item { Spacer(modifier = Modifier.height(28.dp)) }
    }
}

@Composable
private fun ProgressionPlayScreen(animal: FarmAnimal, stars: Int, onBack: () -> Unit, onDone: () -> Unit) {
    val reactions = listOf(
        "Pegou! 🐾",
        "Boa! ⚽",
        "De novo! 😄",
        "Que divertido! 🎉",
        "Muito bem! ⭐"
    )
    var throws by remember(animal.id) { mutableIntStateOf(0) }
    var playFeedback by remember(animal.id) { mutableStateOf("Toque na bola para jogar!") }
    val total = reactions.size
    val finished = throws >= total
    val progress = (throws.toFloat() / total.toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(14.dp)) }
        item { ProgressionTopBar("Brincar", stars, onBack) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = animal.color)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimalAvatar(animal.name, animal.emoji, modifier = Modifier.size(118.dp))
                    Text(
                        if (finished) "${animal.name} adorou brincar!" else playFeedback,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Text("⚽", fontSize = 62.sp)
                    Text(
                        if (finished) "5 de 5 jogadas" else "Jogada ${throws + 1} de $total",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF607060)
                    )
                }
            }
        }
        item {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(12.dp)),
                color = Color(0xFFFF8A65),
                trackColor = Color.White
            )
        }
        item {
            Button(
                onClick = {
                    if (finished) {
                        onDone()
                    } else {
                        playFeedback = reactions[throws]
                        throws += 1
                    }
                },
                modifier = Modifier.fillMaxWidth().height(70.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (finished) Color(0xFF5BAE62) else Color(0xFFFF8A65)
                )
            ) {
                Text(
                    if (finished) "Receber até 2 estrelas" else "⚽ Jogar bola",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
        item { Spacer(modifier = Modifier.height(28.dp)) }
    }
}

@Composable
private fun ProgressionSoundsScreen(
    progression: GameProgression,
    stars: Int,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    val unlockedAnimals = FARM_ANIMALS.filter { progression.isUnlocked(it.id, it.startsUnlocked) }
    val unlockedKey = unlockedAnimals.joinToString("|") { it.id }
    val quizAnimals = remember(unlockedKey) { unlockedAnimals.shuffled() }
    val answerAnimals = remember(unlockedKey) { unlockedAnimals.shuffled() }
    var quizIndex by remember(unlockedKey) { mutableIntStateOf(0) }
    var feedback by remember(unlockedKey) { mutableStateOf("Escute e escolha o bichinho!") }
    val target = quizAnimals.getOrNull(quizIndex)
    val finished = target == null

    LaunchedEffect(quizIndex, quizAnimals.size) {
        target?.let { playAnimalSound(it.name) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(14.dp)) }
        item { ProgressionTopBar("Sons", stars, onBack) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🔊", fontSize = 48.sp)
                    Text(feedback, textAlign = TextAlign.Center, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    if (target != null) {
                        Text(
                            "Som ${quizIndex + 1} de ${quizAnimals.size}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF607060)
                        )
                        Button(onClick = { playAnimalSound(target.name) }) {
                            Text("Ouvir de novo")
                        }
                    }
                }
            }
        }
        if (!finished && target != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    answerAnimals.forEach { animal ->
                        Button(
                            onClick = {
                                if (animal.id == target.id) {
                                    feedback = "Muito bem! ${animal.name}!"
                                    quizIndex += 1
                                } else {
                                    feedback = "Quase! Escute de novo."
                                    playAnimalSound(target.name)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(62.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = animal.color,
                                contentColor = Color(0xFF3E463E)
                            )
                        ) {
                            Text("${animal.emoji}  ${animal.name}", fontSize = 18.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        } else {
            item {
                Button(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5BAE62))
                ) {
                    Text("Receber até 3 estrelas", fontSize = 18.sp, fontWeight = FontWeight.Black)
                }
            }
        }
        item { Spacer(modifier = Modifier.height(28.dp)) }
    }
}

@Composable
private fun ProgressionTopBar(title: String, stars: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = onBack, shape = RoundedCornerShape(18.dp)) { Text("←") }
        Text(title, fontSize = 25.sp, fontWeight = FontWeight.Black, color = Color(0xFF315337))
        Text("⭐ $stars", fontWeight = FontWeight.Black)
    }
}
