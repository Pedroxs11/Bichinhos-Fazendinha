package com.minhafazendinha.game

/**
 * Escopo controlado para aprovacao do novo visual antes de replica-lo.
 * A primeira cena piloto e a rotina de cuidados da vaca; o restante do jogo
 * continua usando o visual atual ate a aprovacao.
 */
object VisualPilot {
    const val enabled = true
    const val sceneId = "cow_care"

    const val title = "Cantinho da Mimosa"
    const val subtitle = "Cuide dela com carinho"

    const val sceneHeight = 560
    const val cardRadius = 34f
    const val sceneRadius = 38f

    const val skyTop = 0xFF9EDCFF.toInt()
    const val skyBottom = 0xFFEAF8FF.toInt()
    const val grassLight = 0xFF8DD46A.toInt()
    const val grassDark = 0xFF5FA94C.toInt()
    const val wood = 0xFFDFA96A.toInt()
    const val woodDark = 0xFF9A6542.toInt()
    const val cream = 0xFFFFF8E8.toInt()
    const val ink = 0xFF4A392D.toInt()
    const val accent = 0xFFFFC95C.toInt()

    fun appliesTo(scene: String): Boolean = enabled && scene == sceneId
}
