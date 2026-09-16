package com.minhafazendinha.game

/**
 * Configuracao central da fabrica de jogos.
 * Conteudo que muda entre temas deve ficar aqui sempre que possivel.
 */
object GameConfig {
    const val TITLE = "🌻 BICHINHOS & FAZENDINHA"
    const val SUBTITLE = "Vamos cuidar dos nossos amigos!"
    const val SOUNDS_TITLE = "🎵 SONS"
    const val SOUNDS_SUBTITLE = "Toque no bichinho e ouça o som!"
    const val CARE_TITLE = "❤️ CUIDAR"
    const val CARE_DONE_TITLE = "⭐ MUITO BEM!"
    const val CARE_DONE_SUBTITLE = "Você cuidou da vaquinha!"
    const val FARM_TITLE = "🚜 FAZENDINHA"
    const val COOP_TITLE = "🐔 GALINHEIRO"
    const val REWARD_TITLE = "🎉 TAREFA CONCLUÍDA!"
    const val WARDROBE_TITLE = "👕 ARMÁRIO"
    const val WARDROBE_SUBTITLE = "Escolha um bichinho e monte o visual!"
    const val BONUS_TITLE = "🦆 LAGO"
    const val BONUS_SUBTITLE = "Área bônus desbloqueada!"
    const val BONUS_LOCK_MESSAGE = "Ganhe estrelas cuidando da fazendinha! ⭐"
    const val HOME_LABEL = "🏠 Voltar ao mapa"
    const val LAKE_UNLOCK_STARS = 15
    const val CARE_STAR_REWARD = 5
    const val CARE_HEART_REWARD = 1
    const val FARM_REWARD = 2
    const val COOP_REWARD = 1

    data class Animal(val emoji: String, val name: String, val key: String)
    data class HomeAction(val icon: String, val title: String, val id: String)
    data class CareStep(val icon: String, val title: String, val instruction: String, val feedback: String)
    data class FarmStep(val icon: String, val title: String, val action: String, val progressIcon: String, val taps: Int, val scene: String)

    val animals = listOf(
        Animal("🐮", "Vaca", "vaca"), Animal("🐔", "Galinha", "galinha"),
        Animal("🐶", "Cachorro", "cachorro"), Animal("🐴", "Cavalo", "cavalo"),
        Animal("🐷", "Porco", "porco"), Animal("🐑", "Ovelha", "ovelha"),
        Animal("🐐", "Cabra", "cabra"), Animal("🫏", "Burro", "burro")
    )

    val homeActions = listOf(
        HomeAction("🔊", "SONS DOS BICHINHOS", "sounds"),
        HomeAction("🐮", "CURRAL — Cuidar", "care"),
        HomeAction("🌱", "HORTA — Regar e colher", "farm"),
        HomeAction("🐔", "GALINHEIRO — Pegar ovos", "coop"),
        HomeAction("👕", "ARMÁRIO — Animais e roupas", "wardrobe")
    )

    val careSteps = listOf(
        CareStep("🍎", "Alimentar", "Dê comida para a vaquinha", "🍎 A maçã está pronta!"),
        CareStep("🛁", "Banho", "Espuma e água para ficar limpinha", "🫧 Banho gostoso!"),
        CareStep("🧻", "Secar", "Seque bem a vaquinha", "✨ Limpinha e cheirosa!"),
        CareStep("🌙", "Dormir", "Hora de descansar", "💤 Boa noite, vaquinha!")
    )

    val farmSteps = listOf(
        FarmStep("💧", "Regar a horta", "REGAR", "💧", 3, "garden"),
        FarmStep("🌽", "Colher os alimentos", "COLHER", "🧺", 3, "garden"),
        FarmStep("🥚", "Pegar os ovos", "PEGAR OVO", "🥚", 4, "coop")
    )

    val accessories = listOf("👒", "🎀", "🧢", "👑", "🕶️", "🧣", "🎩")
}
