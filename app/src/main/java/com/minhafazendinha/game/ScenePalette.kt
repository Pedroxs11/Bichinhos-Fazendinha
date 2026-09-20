package com.minhafazendinha.game

import android.graphics.Color

/** Shared visual tokens for every game produced by the factory. */
object ScenePalette {
    val skyTop = Color.rgb(92, 198, 240)
    val skyBottom = Color.rgb(219, 244, 175)
    val sun = Color.rgb(255, 220, 62)
    val hill = Color.rgb(91, 176, 74)
    val lake = Color.rgb(78, 181, 226)
    val lakeHighlight = Color.rgb(105, 213, 237)
    val soil = Color.rgb(121, 76, 40)
    val fence = Color.rgb(220, 176, 102)
    val fenceRail = Color.rgb(191, 139, 72)
    val plant = Color.rgb(60, 150, 62)
    val crop = Color.rgb(239, 174, 47)
    val coopWall = Color.rgb(230, 173, 76)
    val coopRoof = Color.rgb(181, 62, 48)
    val fish = Color.rgb(255, 154, 42)
    val duck = Color.rgb(255, 242, 201)
    val duckBeak = Color.rgb(255, 166, 37)
    val ink = Color.rgb(45, 61, 70)
    val lily = Color.rgb(72, 167, 75)
    val flower = Color.rgb(244, 119, 171)
    val heart = Color.rgb(255, 92, 145)
}


/** Semantic palette contract: lets future games replace a theme without touching scene code. */
data class GameSceneTheme(
    val skyTop: Int,
    val skyBottom: Int,
    val hill: Int,
    val water: Int,
    val waterHighlight: Int,
    val ground: Int,
    val accent: Int
)

object GameSceneThemes {
    val FARM = GameSceneTheme(
        skyTop = ScenePalette.skyTop,
        skyBottom = ScenePalette.skyBottom,
        hill = ScenePalette.hill,
        water = ScenePalette.lake,
        waterHighlight = ScenePalette.lakeHighlight,
        ground = ScenePalette.soil,
        accent = ScenePalette.flower
    )

    fun validate(theme: GameSceneTheme): List<String> = buildList {
        if (theme.skyTop == theme.skyBottom) add("sky gradient needs two tones")
        if (theme.water == theme.waterHighlight) add("water highlight must contrast with water")
        if (theme.ground == theme.hill) add("ground and hill need visual separation")
    }
}
