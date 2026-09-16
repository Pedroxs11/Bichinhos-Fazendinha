package com.minhafazendinha.game

import android.graphics.Color

/**
 * Tema visual central da fabrica de jogos.
 * O objetivo e permitir que um novo jogo troque identidade visual sem mexer
 * nas regras, progresso ou navegacao.
 */
object GameTheme {
    val skyTop = Color.rgb(98, 201, 243)
    val skyBottom = Color.rgb(221, 245, 183)
    val titleText = Color.rgb(91, 48, 24)
    val bodyText = Color.rgb(90, 75, 55)
    val hudText = Color.rgb(92, 65, 37)

    val primaryButton = Color.rgb(98, 169, 59)
    val primaryButtonBorder = Color.rgb(63, 118, 39)
    val primaryButtonText = Color.WHITE

    val cardBackground = Color.rgb(255, 241, 208)
    val cardBorder = Color.rgb(211, 154, 75)
    val wardrobeBackground = Color.rgb(255, 231, 188)

    const val SCREEN_PADDING = 24
    const val BUTTON_RADIUS = 25f
    const val CARD_RADIUS = 24f
    const val BUTTON_MIN_HEIGHT = 96
    const val BUTTON_TEXT_SIZE = 19f
    const val TITLE_TEXT_SIZE = 29f
    const val SUBTITLE_TEXT_SIZE = 17f
    const val HUD_TEXT_SIZE = 19f
    const val BUTTON_ELEVATION = 7f
    const val TAP_DEBOUNCE_MS = 280L

    fun backgroundColors() = intArrayOf(skyTop, skyBottom)
}
