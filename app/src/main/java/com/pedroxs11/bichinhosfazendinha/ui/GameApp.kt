package com.pedroxs11.bichinhosfazendinha.ui

import android.content.Context
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val GAME_PREFS = "game_progress"
private const val KEY_STARS = "stars"

private data class Animal(
    val name: String,
    val emoji: String,
    val color: Color,
    val sound: String
)

private data class ActivityItem(
    val title: String,
    val emoji: String,
    val subtitle: String,
    val color: Color
)

private data class CareStep(
    val title: String,
    val emoji: String,
    val instruction: String,
    val successMessage: String
)

private data class FarmStep(
    val title: String,
    val emoji: String,
    val instruction: String,
    val successMessage: String
)

private enum class Screen {
    HOME,
    CARE,
    SOUNDS,
    FARM,
    PLAY
}

private val animals = listOf(
    Animal("Vaca", "🐄", Color(0xFFFFF3D8), "Muuu!"),
    Animal("Porquinho", "🐷", Color(0xFFFFDDE8), "Oinc oinc!"),
    Animal("Galinha", "🐔", Color(0xFFFFE7C2), "Có có có!"),
    Animal("Cachorro", "🐶", Color(0xFFE8D8C8), "Au au!")
)

private val activities = listOf(
    ActivityItem("Sons", "🔊", "Escute e descubra os bichinhos", Color(0xFFFFE082)),
    ActivityItem("Cuidar", "🛁", "Comida, banho e carinho", Color(0xFF81D4FA)),
    ActivityItem("Brincar", "⚽", "Jogue bola com seu bichinho", Color(0xFFFFAB91)),
    ActivityItem("Fazendinha", "🌱", "Plante, colha e cuide da fazenda", Color(0xFFA5D6A7))
)

private val careSteps = listOf(
    CareStep("Alimentar", "🍎", "Dê uma comidinha para o bichinho!", "Hummm! Barriguinha cheia!"),
    CareStep("Dar banho", "🛁", "Hora de lavar e tirar toda a sujeira!", "Splash! Agora está limpinho!"),
    CareStep("Secar", "🧻", "Seque bem o bichinho depois do banho!", "Prontinho! Bem sequinho!"),
    CareStep("Dormir", "🌙", "Ajude o bichinho a relaxar para dormir!", "Boa noite! Zzz...")
)

private val farmSteps = listOf(
    FarmStep("Regar a horta", "💧", "Dê água para as plantinhas crescerem!", "A horta ficou verdinha! 🌱"),
    FarmStep("Colher frutas", "🍎", "Pegue as frutas maduras da árvore!", "Cestinha cheia de frutas! 🍎"),
    FarmStep("Pegar ovos", "🥚", "Ajude a recolher os ovos do galinheiro!", "Todos os ovos foram guardados! 🥚")
)

@Composable
fun GameApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(GAME_PREFS, Context.MODE_PRIVATE) }

    var selectedAnimal by remember { mutableStateOf(animals.first()) }
    var stars by remember { mutableIntStateOf(prefs.getInt(KEY_STARS, 0)) }
    var message by remember { mutableStateOf("Escolha um bichinho para começar!") }
    var screen by remember { mutableStateOf(Screen.HOME) }

    fun addStars(amount: Int) {
        stars += amount
        prefs.edit().putInt(KEY_STARS, stars).apply()
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            FarmBackground()

            when (screen) {
                Screen.HOME -> HomeScreen(
                    selectedAnimal = selectedAnimal,
                    stars = stars,
                    message = message,
                    onAnimalSelected = {
                        selectedAnimal = it
                        message = "Oi! Eu sou ${it.name.lowercase()}! Vamos brincar?"
                    },
                    onActivitySelected = { activity ->
                        screen = when (activity.title) {
                            "Cuidar" -> Screen.CARE
                            "Sons" -> Screen.SOUNDS
                            "Fazendinha" -> Screen.FARM
                            "Brincar" -> Screen.PLAY
                            else -> Screen.HOME
                        }
                    }
                )

                Screen.CARE -> CareScreen(
                    animal = selectedAnimal,
                    stars = stars,
                    onBack = { screen = Screen.HOME },
                    onRoutineCompleted = {
                        addStars(5)
                        message = "${selectedAnimal.name} está feliz, limpinho e descansado! +5 ⭐"
                        screen = Screen.HOME
                    }
                )

                Screen.SOUNDS -> SoundsScreen(
                    stars = stars,
                    onBack = { screen = Screen.HOME },
                    onQuizCompleted = {
                        addStars(3)
                        message = "Você descobriu os sons dos bichinhos! +3 ⭐"
                        screen = Screen.HOME
                    }
                )

                Screen.FARM -> FarmScreen(
                    stars = stars,
                    onBack = { screen = Screen.HOME },
                    onFarmCompleted = {
                        addStars(4)
                        message = "A fazendinha está cuidada e cheia de vida! +4 ⭐"
                        screen = Screen.HOME
                    }
                )

                Screen.PLAY -> PlayScreen(
                    animal = selectedAnimal,
                    stars = stars,
                    onBack = { screen = Screen.HOME },
                    onPlayCompleted = {
                        addStars(2)
                        message = "${selectedAnimal.name} adorou brincar com você! +2 ⭐"
                        screen = Screen.HOME
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeScreen(
    selectedAnimal: Animal,
    stars: Int,
    message: String,
    onAnimalSelected: (Animal) -> Unit,
    onActivitySelected: (ActivityItem) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(14.dp)) }
        item { Header(stars = stars) }
        item {
            MessageBubble(
                animalName = selectedAnimal.name,
                emoji = selectedAnimal.emoji,
                message = message
            )
        }
        item { SectionTitle("Escolha seu bichinho") }

        items(animals.chunked(2)) { rowAnimals ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowAnimals.forEach { animal ->
                    AnimalCard(
                        modifier = Modifier.weight(1f),
                        animal = animal,
                        selected = animal == selectedAnimal,
                        onClick = { onAnimalSelected(animal) }
                    )
                }
            }
        }

        item { SectionTitle("O que vamos fazer?") }
        items(activities) { activity ->
            ActivityCard(activity = activity, onPlay = { onActivitySelected(activity) })
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun PlayScreen(
    animal: Animal,
    stars: Int,
    onBack: () -> Unit,
    onPlayCompleted: () -> Unit
) {
    var throws by remember(animal.name) { mutableIntStateOf(0) }
    var feedback by remember(animal.name) { mutableStateOf("Toque na bola para jogar com ${animal.name.lowercase()}!") }
    val totalThrows = 5
    val finished = throws >= totalThrows
    val progress = (throws.toFloat() / totalThrows.toFloat()).coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(14.dp)) }
        item { TopBar(title = "Brincar ⚽", stars = stars, onBack = onBack) }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFE1D6))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnimalAvatar(
                        animalName = animal.name,
                        fallbackEmoji = animal.emoji,
                        modifier = Modifier.size(112.dp)
                    )
                    Text(feedback, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = Color(0xFF5E514B))
                    if (finished) {
                        Text("🎉", fontSize = 82.sp)
                    } else {
                        ItemArt(title = "Brincar", fallbackEmoji = "⚽", modifier = Modifier.size(86.dp))
                    }
                }
            }
        }

        item {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(12.dp)),
                color = Color(0xFFFF8A65),
                trackColor = Color.White.copy(alpha = 0.8f)
            )
        }

        if (!finished) {
            item {
                Button(
                    onClick = {
                        throws += 1
                        feedback = when (throws) {
                            1 -> "Boa! ${animal.name} pegou a bola!"
                            2 -> "Mais uma! ⚽"
                            3 -> "Que divertido!"
                            4 -> "Só falta uma!"
                            else -> "Muito bem! Brincadeira completa! 🎉"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(78.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8A65))
                ) {
                    Text("⚽ Jogar bola  ${throws + 1}/$totalThrows", fontSize = 21.sp, fontWeight = FontWeight.Black)
                }
            }
        } else {
            item {
                RewardCard(
                    text = "Você jogou a bola 5 vezes e ganhou 2 estrelas!",
                    buttonText = "⭐ Receber 2 estrelas",
                    onClick = onPlayCompleted
                )
            }
        }

        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun FarmScreen(
    stars: Int,
    onBack: () -> Unit,
    onFarmCompleted: () -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf("Vamos ajudar na fazendinha!") }
    var actionTaps by remember(currentStep) { mutableIntStateOf(0) }
    val requiredTaps = 4
    val finished = currentStep >= farmSteps.size
    val progress = if (finished) 1f else currentStep.toFloat() / farmSteps.size.toFloat()
    val sceneEmoji = when (currentStep) {
        0 -> "🌱"
        1 -> "🌳"
        2 -> "🐔"
        else -> "🏡"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(14.dp)) }
        item { TopBar(title = "Fazendinha 🌾", stars = stars, onBack = onBack) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDDF3D5))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(sceneEmoji, fontSize = 92.sp)
                    Text(
                        if (finished) "Tudo pronto!" else farmSteps[currentStep].title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF3E5F3C)
                    )
                    Text(feedback, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, color = Color(0xFF536653))
                }
            }
        }
        item {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(12.dp)),
                color = Color(0xFF5BAE62),
                trackColor = Color.White.copy(alpha = 0.8f)
            )
        }

        if (!finished) {
            val step = farmSteps[currentStep]
            item {
                TapActionPanel(
                    emoji = step.emoji,
                    title = step.title,
                    instruction = step.instruction,
                    taps = actionTaps,
                    requiredTaps = requiredTaps,
                    onTap = {
                        val next = actionTaps + 1
                        actionTaps = next
                        if (next >= requiredTaps) {
                            feedback = step.successMessage
                            currentStep += 1
                        } else {
                            feedback = "Continue! ${requiredTaps - next} toque(s) para terminar."
                        }
                    }
                )
            }
        } else {
            item {
                RewardCard(
                    text = "Você cuidou da fazendinha e ganhou 4 estrelas!",
                    buttonText = "⭐ Receber 4 estrelas",
                    onClick = onFarmCompleted
                )
            }
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun SoundsScreen(stars: Int, onBack: () -> Unit, onQuizCompleted: () -> Unit) {
    var heardAnimal by remember { mutableStateOf<Animal?>(null) }
    var quizIndex by remember { mutableIntStateOf(0) }
    var feedback by remember { mutableStateOf("Toque em um bichinho para descobrir o som!") }
    var correctAnswers by remember { mutableIntStateOf(0) }
    val quizFinished = quizIndex >= animals.size
    val target = if (quizFinished) null else animals[quizIndex]

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(14.dp)) }
        item { TopBar(title = "Sons 🔊", stars = stars, onBack = onBack) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🎵", fontSize = 52.sp)
                    Text(feedback, textAlign = TextAlign.Center, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    heardAnimal?.let {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AnimalAvatar(
                                animalName = it.name,
                                fallbackEmoji = it.emoji,
                                compact = true,
                                modifier = Modifier.size(44.dp)
                            )
                            Text(it.sound, fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }
        }
        item { SectionTitle("Descubra os sons") }
        items(animals.chunked(2)) { rowAnimals ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowAnimals.forEach { animal ->
                    Card(
                        modifier = Modifier.weight(1f).height(135.dp).clickable {
                            heardAnimal = animal
                            feedback = "Esse é o som da ${animal.name.lowercase()}!"
                        },
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = animal.color)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            AnimalArt(
                                animalName = animal.name,
                                fallbackEmoji = animal.emoji,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(animal.name, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Text("🔊 tocar", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
        item { SectionTitle("Qual bichinho faz esse som?") }
        if (!quizFinished && target != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4B8))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🔊", fontSize = 54.sp)
                        Text(target.sound, fontSize = 30.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
            items(animals.chunked(2)) { rowAnimals ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowAnimals.forEach { animal ->
                        Button(
                            modifier = Modifier.weight(1f).height(70.dp),
                            onClick = {
                                if (animal == target) {
                                    correctAnswers += 1
                                    feedback = "Muito bem! ${animal.emoji}"
                                    quizIndex += 1
                                } else {
                                    feedback = "Quase! Tente outro bichinho 😊"
                                }
                            },
                            shape = RoundedCornerShape(22.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = animal.color, contentColor = Color(0xFF3E463E))
                        ) {
                            Text("${animal.emoji} ${animal.name}", fontSize = 17.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        } else {
            item {
                RewardCard(
                    text = "Você acertou $correctAnswers de ${animals.size} e ganhou 3 estrelas!",
                    buttonText = "⭐ Receber 3 estrelas",
                    onClick = onQuizCompleted
                )
            }
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun CareScreen(
    animal: Animal,
    stars: Int,
    onBack: () -> Unit,
    onRoutineCompleted: () -> Unit
) {
    var currentStep by remember(animal.name) { mutableIntStateOf(0) }
    var feedback by remember(animal.name) { mutableStateOf("Vamos cuidar de ${animal.name.lowercase()}!") }
    var actionTaps by remember(animal.name, currentStep) { mutableIntStateOf(0) }
    val requiredTaps = 3
    val finished = currentStep >= careSteps.size
    val progress = if (finished) 1f else currentStep.toFloat() / careSteps.size.toFloat()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(14.dp)) }
        item { TopBar(title = "Cuidar ${animal.emoji}", stars = stars, onBack = onBack) }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = animal.color)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AnimalAvatar(
                        animalName = animal.name,
                        fallbackEmoji = animal.emoji,
                        modifier = Modifier.size(118.dp)
                    )
                    Text(animal.name, fontSize = 26.sp, fontWeight = FontWeight.Black)
                    Text(feedback, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                }
            }
        }
        item {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(12.dp)),
                color = Color(0xFF5BAE62),
                trackColor = Color.White.copy(alpha = 0.8f)
            )
        }
        if (!finished) {
            val step = careSteps[currentStep]
            item {
                TapActionPanel(
                    emoji = step.emoji,
                    title = step.title,
                    instruction = step.instruction,
                    taps = actionTaps,
                    requiredTaps = requiredTaps,
                    onTap = {
                        val next = actionTaps + 1
                        actionTaps = next
                        if (next >= requiredTaps) {
                            feedback = step.successMessage
                            currentStep += 1
                        } else {
                            feedback = "Muito bem! Mais ${requiredTaps - next} toque(s)."
                        }
                    }
                )
            }
        } else {
            item {
                RewardCard(
                    text = "Você completou todos os cuidados e ganhou 5 estrelas!",
                    buttonText = "⭐ Receber 5 estrelas",
                    onClick = onRoutineCompleted
                )
            }
        }
        item { Spacer(modifier = Modifier.height(30.dp)) }
    }
}

@Composable
private fun TopBar(title: String, stars: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onBack,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
        ) {
            Text("←", color = Color(0xFF315337), fontSize = 22.sp)
        }
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color(0xFF315337))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.95f))
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Text("⭐ $stars", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RewardCard(text: String, buttonText: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4B8))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("🎉", fontSize = 62.sp)
            Text("Muito bem!", fontSize = 28.sp, fontWeight = FontWeight.Black)
            Text(text, fontSize = 17.sp, textAlign = TextAlign.Center)
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().height(62.dp),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5BAE62))
            ) {
                Text(buttonText, fontSize = 18.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun FarmBackground() {
    Canvas(modifier = Modifier.fillMaxSize().background(Color(0xFFBDEBFF))) {
        drawCircle(color = Color(0xFFFFE66D), radius = 60f, center = Offset(size.width - 90f, 90f))
        drawRect(
            color = Color(0xFF9AD66D),
            topLeft = Offset(0f, size.height * 0.55f),
            size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.45f)
        )
        drawCircle(color = Color.White.copy(alpha = 0.85f), radius = 40f, center = Offset(70f, 110f))
        drawCircle(color = Color.White.copy(alpha = 0.85f), radius = 30f, center = Offset(110f, 110f))
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
            Text("Bichinhos", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color(0xFF31613A))
            Text("& Fazendinha", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4C8B55))
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White.copy(alpha = 0.95f))
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Text("⭐ $stars", fontSize = 21.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MessageBubble(animalName: String, emoji: String, message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(62.dp).clip(CircleShape).background(Color(0xFFFFF5D6)),
                contentAlignment = Alignment.Center
            ) {
                AnimalAvatar(
                    animalName = animalName,
                    fallbackEmoji = emoji,
                    compact = true,
                    modifier = Modifier.size(52.dp)
                )
            }
            Text(
                message,
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
        text,
        modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
        fontSize = 22.sp,
        fontWeight = FontWeight.Black,
        color = Color(0xFF315337)
    )
}

@Composable
private fun AnimalCard(modifier: Modifier, animal: Animal, selected: Boolean, onClick: () -> Unit) {
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
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimalArt(
                animalName = animal.name,
                fallbackEmoji = animal.emoji,
                modifier = Modifier.size(72.dp)
            )
            Text(animal.name, fontSize = 20.sp, fontWeight = FontWeight.Black)
            if (selected) {
                Text("✓ escolhido", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
            }
        }
    }
}

@Composable
private fun ActivityCard(activity: ActivityItem, onPlay: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.96f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(activity.color),
                contentAlignment = Alignment.Center
            ) {
                ItemArt(
                    title = activity.title,
                    fallbackEmoji = activity.emoji,
                    modifier = Modifier.size(42.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(activity.title, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(activity.subtitle, fontSize = 14.sp, color = Color(0xFF607060))
            }
            Button(
                onClick = onPlay,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5BAE62))
            ) {
                Text("▶", fontSize = 20.sp)
            }
        }
    }
}
