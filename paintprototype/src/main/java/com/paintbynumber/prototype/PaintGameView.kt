package com.paintbynumber.prototype

import android.content.Context
import android.content.SharedPreferences
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

class PaintGameView(context: Context) : View(context) {

    data class Area(val number: Int, val path: Path, var painted: Boolean = false, var creativeColor: Int? = null)
    private val colors = listOf(
        Color.rgb(244,67,54), Color.rgb(255,193,7), Color.rgb(76,175,80), Color.rgb(33,150,243),
        Color.rgb(156,39,176), Color.rgb(255,152,0), Color.rgb(0,188,212), Color.rgb(233,30,99),
        Color.rgb(121,85,72), Color.rgb(63,81,181), Color.rgb(0,150,136), Color.rgb(205,220,57),
        Color.rgb(255,87,34), Color.rgb(103,58,183), Color.rgb(3,169,244), Color.rgb(139,195,74),
        Color.rgb(255,193,7), Color.rgb(96,125,139), Color.rgb(233,30,99), Color.rgb(33,33,33)
    )
    private val specialColors = listOf(Color.rgb(212,175,55), Color.rgb(192,192,192), Color.rgb(57,255,20))
    private var selected = 1
    private var selectedSpecial = -1
    private fun specialColorsUnlocked(): Boolean = prefs.getBoolean("special_colors_unlocked", false)
    private fun unlockSpecialColors(): Boolean {
        if (specialColorsUnlocked() || ticketCount() <= 0) return false
        prefs.edit().putInt("reward_tickets", ticketCount() - 1).putBoolean("special_colors_unlocked", true).apply()
        rewardTitle = "Cores especiais liberadas! ✨"
        rewardUntil = System.currentTimeMillis() + 2000L
        invalidate()
        return true
    }
    private var creativeMode = false
    private var galleryMode = true
    private var galleryScroll = 0f
    private var galleryCategory = 0
    private var achievementsMode = false
    private var hintUntil = 0L
    private var celebrationParticles = emptyList<Pair<Float,Float>>()
    private var completionCardVisible = false
    private var completionRewardShown = false
    private val galleryCategories = listOf("Todos", "Fáceis", "Desafios", "Concluídos", "Criativos")
    private var galleryDownY = 0f
    private var galleryStartScroll = 0f
    private var drawingIndex = 0
    private var areas = mutableListOf<Area>()
    private var wrongArea: Area? = null
    private var scaleFactor = 1f
    private var offsetX = 0f
    private var offsetY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var dragging = false
    private var rewardUntil = 0L
    private var rewardTitle = ""
    private val prefs: SharedPreferences = context.getSharedPreferences("paint_progress", Context.MODE_PRIVATE)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    init { setBackgroundColor(Color.WHITE) }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) = rebuild()

    private val drawingNames = listOf("Borboleta", "Peixinho", "Tartaruga", "Foguete", "Flor", "Sorvete", "Quebra-cabeça", "Mundo Mágico", "Arco-íris", "Leãozinho", "Dinossauro")
    // Monetization prototype: first four are free; one rewarded-video action
    // unlocks the next two drawings. Real ad SDK will replace this simulator later.
    private fun unlockedDrawingCount(): Int = prefs.getInt("unlocked_drawing_count", 4).coerceAtMost(drawingNames.size)
    private fun simulateRewardedVideoUnlock() {
        val current = unlockedDrawingCount()
        prefs.edit().putInt("unlocked_drawing_count", (current + 2).coerceAtMost(drawingNames.size)).apply()
        rewardTitle = "2 novas artes liberadas!"
        rewardUntil = System.currentTimeMillis() + 2200L
        completionCardVisible = true
        invalidate()
    }
    private fun ticketCount(): Int = prefs.getInt("reward_tickets", 0)
    private fun earnRewardTicket() {
        prefs.edit().putInt("reward_tickets", ticketCount() + 1).apply()
        rewardTitle = "Ticket ganho! 🎟"
        rewardUntil = System.currentTimeMillis() + 1800L
        invalidate()
    }
    private fun spendTicketToUnlock(): Boolean {
        if (ticketCount() <= 0 || unlockedDrawingCount() >= drawingNames.size) return false
        prefs.edit()
            .putInt("reward_tickets", ticketCount() - 1)
            .putInt("unlocked_drawing_count", (unlockedDrawingCount() + 2).coerceAtMost(drawingNames.size))
            .apply()
        rewardTitle = "Ticket usado • 2 artes liberadas!"
        rewardUntil = System.currentTimeMillis() + 2000L
        invalidate()
        return true
    }

    private fun rebuild() {
        areas = when (drawingIndex) {
            0 -> butterfly()
            1 -> fish()
            2 -> turtle()
            3 -> rocket()
            4 -> flower()
            5 -> iceCream()
            6 -> mosaic()
            7 -> landscape()
            8 -> rainbow()
            9 -> lionCub()
            else -> dinosaur()
        }
        restoreProgress()
        completionCardVisible = isDrawingComplete()
        completionRewardShown = isDrawingComplete()
        selected = nextIncompleteNumber() ?: (areas.firstOrNull()?.number ?: 1)
        wrongArea = null
        scaleFactor = 1f
        offsetX = 0f
        offsetY = 0f
        invalidate()
    }


    private fun clampPan() {
        if (scaleFactor <= 1f) {
            offsetX = 0f
            offsetY = 0f
            return
        }
        val maxX = width * (scaleFactor - 1f) * .50f
        val maxY = height * (scaleFactor - 1f) * .32f
        offsetX = offsetX.coerceIn(-maxX, maxX)
        offsetY = offsetY.coerceIn(-maxY, maxY)
    }

    private fun progressKey() = "drawing_${drawingIndex}_" + (if (creativeMode) "creative" else "numbers")

    private fun saveProgress() {
        val painted = areas.mapIndexedNotNull { index, area -> if (area.painted) index.toString() else null }.joinToString(",")
        val creative = areas.mapIndexedNotNull { index, area ->
            area.creativeColor?.let { "$index:$it" }
        }.joinToString(",")
        prefs.edit()
            .putString("${progressKey()}_painted", painted)
            .putString("${progressKey()}_colors", creative)
            .putBoolean("${progressKey()}_complete", isDrawingComplete())
            .apply()
        if (isDrawingComplete()) completionCardVisible = true
    }

    private fun restoreProgress() {
        val paintedIndexes = prefs.getString("${progressKey()}_painted", "") ?: ""
        paintedIndexes.split(",").mapNotNull { it.toIntOrNull() }.forEach { index ->
            areas.getOrNull(index)?.painted = true
        }
        val creative = prefs.getString("${progressKey()}_colors", "") ?: ""
        creative.split(",").forEach { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val index = parts[0].toIntOrNull()
                val color = parts[1].toIntOrNull()
                if (index != null && color != null) {
                    areas.getOrNull(index)?.apply {
                        painted = true
                        creativeColor = color
                    }
                }
            }
        }
    }

    private fun drawingRegionCount(index: Int): Int = when(index) {
        6 -> 25
        7 -> 23
        8 -> 18
        9 -> 20
        10 -> 22
        else -> 8
    }

    private fun savedProgress(index: Int): Int {
        val prefix = "drawing_${index}_numbers"
        if (prefs.getBoolean("${prefix}_complete", false)) return 100
        val painted = prefs.getString("${prefix}_painted", "") ?: ""
        val count = painted.split(",").count { it.isNotBlank() }
        return (count * 100 / drawingRegionCount(index)).coerceIn(0, 100)
    }

    private fun savedCreativeProgress(index: Int): Int {
        val prefix = "drawing_${index}_creative"
        if (prefs.getBoolean("${prefix}_complete", false)) return 100
        val painted = prefs.getString("${prefix}_painted", "") ?: ""
        val count = painted.split(",").count { it.isNotBlank() }
        return (count * 100 / drawingRegionCount(index)).coerceIn(0, 100)
    }

    private fun isNumberComplete(number: Int): Boolean {
        val matching = areas.filter { it.number == number }
        return matching.isNotEmpty() && matching.all { it.painted }
    }

    private fun nextIncompleteNumber(): Int? =
        areas.map { it.number }.distinct().sorted().firstOrNull { n -> areas.any { it.number == n && !it.painted } }

    private fun isDrawingComplete(): Boolean = areas.isNotEmpty() && areas.all { it.painted }

    private fun completedDrawings(): Int =
        drawingNames.indices.count { index ->
            prefs.getBoolean("drawing_${index}_ever_completed", false) ||
                prefs.getBoolean("drawing_${index}_numbers_complete", false)
        }

    private fun totalCompletedPaintings(): Int = prefs.getInt("total_completed_paintings", 0)

    private fun recordCompletionOnce() {
        val key = "drawing_${drawingIndex}_completion_counted"
        val editor = prefs.edit()
            .putBoolean("drawing_${drawingIndex}_ever_completed", true)
        if (!prefs.getBoolean(key, false)) {
            editor
                .putInt("total_completed_paintings", totalCompletedPaintings() + 1)
                .putBoolean(key, true)
        }
        editor.apply()
        syncAchievementHistory()
    }

    private fun repaintCurrentDrawing() {
        val prefix = "drawing_${drawingIndex}_numbers"
        prefs.edit()
            .remove("${prefix}_painted")
            .remove("${prefix}_colors")
            .remove("${prefix}_complete")
            .remove("drawing_${drawingIndex}_completion_counted")
            .apply()
        creativeMode = false
        completionCardVisible = false
        completionRewardShown = false
        celebrationParticles = emptyList()
        rebuild()
    }

    private fun recordCreativeCompletionOnce() {
        val key = "drawing_${drawingIndex}_creative_completion_counted"
        val everKey = "drawing_${drawingIndex}_creative_ever_completed"
        val editor = prefs.edit().putBoolean(everKey, true)
        if (!prefs.getBoolean(key, false)) {
            editor
                .putInt("total_creative_completed", prefs.getInt("total_creative_completed", 0) + 1)
                .putBoolean(key, true)
        }
        editor.apply()
        syncAchievementHistory()
    }

    private fun creativeCompletedDrawings(): Int =
        drawingNames.indices.count { index ->
            prefs.getBoolean("drawing_${index}_creative_ever_completed", false) ||
                prefs.getBoolean("drawing_${index}_creative_complete", false)
        }

    private fun shareArtwork() {
        // Render only the artwork itself (without buttons, palette or HUD) so
        // the shared PNG looks like a finished picture instead of a screenshot.
        val bounds = RectF()
        areas.forEach { area ->
            val areaBounds = RectF()
            area.path.computeBounds(areaBounds, true)
            if (bounds.isEmpty) bounds.set(areaBounds) else bounds.union(areaBounds)
        }
        if (bounds.isEmpty) return

        val padding = 48f
        val bitmap = Bitmap.createBitmap(
            (bounds.width() + padding * 2).toInt().coerceAtLeast(1),
            (bounds.height() + padding * 2).toInt().coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        canvas.translate(padding - bounds.left, padding - bounds.top)

        areas.forEach { area ->
            paint.style = Paint.Style.FILL
            paint.color = if (creativeMode) {
                area.creativeColor ?: colors[area.number - 1]
            } else {
                colors[area.number - 1]
            }
            canvas.drawPath(area.path, paint)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 3f
            paint.color = Color.rgb(100, 100, 100)
            canvas.drawPath(area.path, paint)
        }

        ArtworkShareHelper.share(context, bitmap, drawingNames[drawingIndex])
    }

    private fun triggerCompletionReward() {
        if (completionRewardShown) return
        completionRewardShown = true
        completionCardVisible = true
        recordCompletionOnce()
        celebrationParticles = List(28) { i ->
            val x = ((i * 37) % 100) / 100f
            val y = ((i * 61) % 70) / 100f + .12f
            Pair(x,y)
        }
        val completed = completedDrawings()
        val total = totalCompletedPaintings()
        val achievementCandidates = listOf(
            Triple("mestre_das_cores", completed >= drawingNames.size, "Mestre das Cores! 👑"),
            Triple("super_pintor", total >= 10, "Super Pintor! 🎨"),
            Triple("pequeno_artista", completed >= 3, "Pequeno Artista! 🏆"),
            Triple("minha_primeira_arte", completed >= 1, "Minha Primeira Arte! ⭐")
        )
        val newlyUnlocked = achievementCandidates.firstOrNull { (key, reached, _) ->
            reached && !prefs.getBoolean("achievement_${key}_celebrated", false)
        }
        if (newlyUnlocked != null) {
            prefs.edit().putBoolean("achievement_${newlyUnlocked.first}_celebrated", true).apply()
            rewardTitle = newlyUnlocked.third
        } else {
            rewardTitle = "Pintura pronta! Você ganhou uma estrelinha ⭐"
        }
        rewardUntil = System.currentTimeMillis() + 2200L
    }

    private fun p(vararg pts: Float): Path {
        val path = Path()
        path.moveTo(pts[0], pts[1])
        var i = 2
        while (i < pts.size) { path.lineTo(pts[i], pts[i+1]); i += 2 }
        path.close()
        return path
    }

    private fun butterfly(): MutableList<Area> {
        val w = width.toFloat(); val top = height * .14f; val bottom = height * .72f
        fun sx(x: Float) = x * w
        fun sy(y: Float) = top + y * (bottom - top)
        return mutableListOf(
            Area(1,p(sx(.48f),sy(.10f),sx(.52f),sy(.10f),sx(.53f),sy(.80f),sx(.47f),sy(.80f))),
            Area(2,p(sx(.47f),sy(.18f),sx(.25f),sy(.05f),sx(.12f),sy(.22f),sx(.28f),sy(.42f))),
            Area(3,p(sx(.28f),sy(.42f),sx(.12f),sy(.22f),sx(.10f),sy(.52f),sx(.31f),sy(.58f))),
            Area(4,p(sx(.31f),sy(.58f),sx(.10f),sy(.52f),sx(.20f),sy(.82f),sx(.43f),sy(.72f))),
            Area(5,p(sx(.53f),sy(.18f),sx(.75f),sy(.05f),sx(.88f),sy(.22f),sx(.72f),sy(.42f))),
            Area(6,p(sx(.72f),sy(.42f),sx(.88f),sy(.22f),sx(.90f),sy(.52f),sx(.69f),sy(.58f))),
            Area(7,p(sx(.69f),sy(.58f),sx(.90f),sy(.52f),sx(.80f),sy(.82f),sx(.57f),sy(.72f))),
            Area(8,p(sx(.43f),sy(.72f),sx(.47f),sy(.80f),sx(.50f),sy(.95f),sx(.53f),sy(.80f),sx(.57f),sy(.72f)))
        )
    }

    private fun fish(): MutableList<Area> {
        val w = width.toFloat(); val top = height*.18f; val bottom = height*.70f
        fun sx(x: Float) = x * w
        fun sy(y: Float) = top + y * (bottom - top)
        return mutableListOf(
            Area(1,p(sx(.18f),sy(.50f),sx(.34f),sy(.22f),sx(.45f),sy(.18f),sx(.42f),sy(.82f),sx(.30f),sy(.77f))),
            Area(2,p(sx(.42f),sy(.18f),sx(.52f),sy(.15f),sx(.52f),sy(.85f),sx(.42f),sy(.82f))),
            Area(3,p(sx(.52f),sy(.15f),sx(.62f),sy(.18f),sx(.63f),sy(.82f),sx(.52f),sy(.85f))),
            Area(4,p(sx(.62f),sy(.18f),sx(.74f),sy(.28f),sx(.78f),sy(.68f),sx(.63f),sy(.82f))),
            Area(5,p(sx(.78f),sy(.68f),sx(.92f),sy(.88f),sx(.88f),sy(.52f),sx(.92f),sy(.16f),sx(.74f),sy(.28f))),
            Area(6,p(sx(.36f),sy(.24f),sx(.48f),sy(.02f),sx(.58f),sy(.18f))),
            Area(7,p(sx(.38f),sy(.76f),sx(.49f),sy(.98f),sx(.60f),sy(.82f))),
            Area(8,p(sx(.22f),sy(.50f),sx(.30f),sy(.42f),sx(.30f),sy(.58f)))
        )
    }

    private fun turtle(): MutableList<Area> {
        val w=width.toFloat(); val top=height*.18f; val bottom=height*.70f
        fun sx(x: Float) = x * w
        fun sy(y: Float) = top + y * (bottom - top)
        return mutableListOf(
            Area(1,p(sx(.28f),sy(.35f),sx(.42f),sy(.20f),sx(.58f),sy(.20f),sx(.72f),sy(.35f),sx(.68f),sy(.68f),sx(.32f),sy(.68f))),
            Area(2,p(sx(.72f),sy(.38f),sx(.88f),sy(.43f),sx(.91f),sy(.55f),sx(.72f),sy(.58f))),
            Area(3,p(sx(.30f),sy(.38f),sx(.18f),sy(.28f),sx(.12f),sy(.40f),sx(.28f),sy(.50f))),
            Area(4,p(sx(.34f),sy(.64f),sx(.24f),sy(.78f),sx(.38f),sy(.80f),sx(.45f),sy(.67f))),
            Area(5,p(sx(.58f),sy(.67f),sx(.66f),sy(.80f),sx(.80f),sy(.76f),sx(.68f),sy(.62f))),
            Area(6,p(sx(.40f),sy(.30f),sx(.50f),sy(.22f),sx(.50f),sy(.45f))),
            Area(7,p(sx(.50f),sy(.22f),sx(.61f),sy(.31f),sx(.50f),sy(.45f))),
            Area(8,p(sx(.40f),sy(.30f),sx(.50f),sy(.45f),sx(.39f),sy(.57f)))
        )
    }

    private fun rocket(): MutableList<Area> {
        val w=width.toFloat(); val top=height*.16f; val bottom=height*.72f
        fun sx(x: Float) = x * w
        fun sy(y: Float) = top + y * (bottom - top)
        return mutableListOf(
            Area(1,p(sx(.50f),sy(.05f),sx(.62f),sy(.28f),sx(.62f),sy(.67f),sx(.38f),sy(.67f),sx(.38f),sy(.28f))),
            Area(2,p(sx(.38f),sy(.48f),sx(.25f),sy(.66f),sx(.38f),sy(.62f))),
            Area(3,p(sx(.62f),sy(.48f),sx(.75f),sy(.66f),sx(.62f),sy(.62f))),
            Area(4,p(sx(.43f),sy(.32f),sx(.57f),sy(.32f),sx(.57f),sy(.47f),sx(.43f),sy(.47f))),
            Area(5,p(sx(.43f),sy(.67f),sx(.50f),sy(.88f),sx(.57f),sy(.67f))),
            Area(6,p(sx(.38f),sy(.28f),sx(.50f),sy(.05f),sx(.62f),sy(.28f))),
            Area(7,p(sx(.45f),sy(.67f),sx(.50f),sy(.78f),sx(.55f),sy(.67f))),
            Area(8,p(sx(.46f),sy(.35f),sx(.54f),sy(.35f),sx(.54f),sy(.44f),sx(.46f),sy(.44f)))
        )
    }

    private fun flower(): MutableList<Area> {
        val w=width.toFloat(); val top=height*.16f; val bottom=height*.72f
        fun sx(x: Float) = x * w
        fun sy(y: Float) = top + y * (bottom - top)
        return mutableListOf(
            Area(1,p(sx(.45f),sy(.38f),sx(.50f),sy(.16f),sx(.55f),sy(.38f),sx(.50f),sy(.48f))),
            Area(2,p(sx(.52f),sy(.40f),sx(.70f),sy(.28f),sx(.65f),sy(.48f),sx(.53f),sy(.50f))),
            Area(3,p(sx(.54f),sy(.50f),sx(.70f),sy(.58f),sx(.55f),sy(.66f),sx(.49f),sy(.53f))),
            Area(4,p(sx(.47f),sy(.52f),sx(.45f),sy(.70f),sx(.34f),sy(.58f),sx(.43f),sy(.49f))),
            Area(5,p(sx(.43f),sy(.47f),sx(.29f),sy(.36f),sx(.45f),sy(.34f),sx(.50f),sy(.45f))),
            Area(6,p(sx(.45f),sy(.43f),sx(.55f),sy(.43f),sx(.57f),sy(.53f),sx(.48f),sy(.56f),sx(.42f),sy(.49f))),
            Area(7,p(sx(.48f),sy(.55f),sx(.52f),sy(.55f),sx(.54f),sy(.91f),sx(.48f),sy(.91f))),
            Area(8,p(sx(.51f),sy(.72f),sx(.68f),sy(.65f),sx(.62f),sy(.83f),sx(.52f),sy(.84f)))
        )
    }

    private fun iceCream(): MutableList<Area> {
        val w=width.toFloat(); val top=height*.16f; val bottom=height*.72f
        fun sx(x: Float) = x * w
        fun sy(y: Float) = top + y * (bottom - top)
        return mutableListOf(
            Area(1,p(sx(.35f),sy(.42f),sx(.65f),sy(.42f),sx(.56f),sy(.88f),sx(.44f),sy(.88f))),
            Area(2,p(sx(.35f),sy(.42f),sx(.30f),sy(.32f),sx(.38f),sy(.18f),sx(.48f),sy(.28f),sx(.50f),sy(.42f))),
            Area(3,p(sx(.50f),sy(.42f),sx(.48f),sy(.28f),sx(.57f),sy(.16f),sx(.67f),sy(.30f),sx(.65f),sy(.42f))),
            Area(4,p(sx(.38f),sy(.42f),sx(.50f),sy(.55f),sx(.44f),sy(.70f))),
            Area(5,p(sx(.50f),sy(.55f),sx(.62f),sy(.42f),sx(.56f),sy(.70f))),
            Area(6,p(sx(.44f),sy(.70f),sx(.50f),sy(.55f),sx(.56f),sy(.70f),sx(.50f),sy(.86f))),
            Area(7,p(sx(.38f),sy(.18f),sx(.43f),sy(.08f),sx(.48f),sy(.28f))),
            Area(8,p(sx(.57f),sy(.16f),sx(.61f),sy(.07f),sx(.65f),sy(.23f)))
        )
    }



    private fun landscape(): MutableList<Area> {
        fun poly(vararg pts: Float): Path = Path().apply {
            moveTo(pts[0]*width, pts[1]*height)
            var i=2
            while(i<pts.size){ lineTo(pts[i]*width, pts[i+1]*height); i+=2 }
            close()
        }
        return mutableListOf(
            Area(15, poly(.12f,.18f,.88f,.18f,.88f,.31f,.12f,.31f)), // sky
            Area(10, poly(.12f,.31f,.30f,.20f,.45f,.31f)), Area(18, poly(.30f,.31f,.49f,.17f,.66f,.31f)),
            Area(10, poly(.49f,.31f,.70f,.21f,.88f,.31f)), // mountains
            Area(1, poly(.12f,.31f,.88f,.31f,.88f,.39f,.12f,.39f)), // sunset band
            Area(2, poly(.12f,.39f,.88f,.39f,.88f,.48f,.12f,.48f)),
            Area(16, poly(.12f,.48f,.88f,.48f,.88f,.57f,.12f,.57f)), // meadow
            Area(3, poly(.12f,.57f,.88f,.57f,.88f,.66f,.12f,.66f)),
            Area(11, poly(.42f,.48f,.58f,.48f,.67f,.66f,.33f,.66f)), // river
            Area(7, poly(.46f,.48f,.54f,.48f,.58f,.66f,.42f,.66f)),
            Area(9, poly(.16f,.42f,.20f,.31f,.24f,.42f)), Area(3, poly(.14f,.48f,.20f,.36f,.27f,.48f)),
            Area(9, poly(.73f,.43f,.78f,.30f,.83f,.43f)), Area(3, poly(.70f,.49f,.78f,.35f,.87f,.49f)),
            Area(6, poly(.18f,.55f,.20f,.48f,.22f,.55f)), Area(6, poly(.77f,.56f,.79f,.47f,.81f,.56f)),
            Area(5, poly(.27f,.60f,.30f,.55f,.33f,.60f)), Area(8, poly(.67f,.61f,.70f,.55f,.73f,.61f)),
            Area(12, poly(.36f,.57f,.39f,.52f,.42f,.57f)), Area(4, poly(.58f,.58f,.61f,.52f,.64f,.58f)),
            Area(17, poly(.12f,.66f,.33f,.66f,.30f,.72f,.12f,.72f)),
            Area(14, poly(.33f,.66f,.67f,.66f,.72f,.72f,.28f,.72f)),
            Area(17, poly(.67f,.66f,.88f,.66f,.88f,.72f,.72f,.72f))
        )
    }

    private fun rainbow(): MutableList<Area> {
        fun poly(vararg pts: Float): Path = Path().apply {
            moveTo(pts[0]*width, pts[1]*height)
            var i=2
            while(i<pts.size){ lineTo(pts[i]*width, pts[i+1]*height); i+=2 }
            close()
        }
        return mutableListOf(
            Area(4, poly(.10f,.18f,.90f,.18f,.90f,.29f,.10f,.29f)),
            Area(15, poly(.10f,.29f,.90f,.29f,.90f,.40f,.10f,.40f)),
            Area(1, poly(.18f,.51f,.24f,.34f,.32f,.25f,.40f,.20f,.48f,.18f,.56f,.20f,.64f,.25f,.72f,.34f,.78f,.51f,.70f,.51f,.65f,.38f,.58f,.31f,.50f,.28f,.42f,.31f,.35f,.38f,.30f,.51f)),
            Area(6, poly(.25f,.51f,.30f,.38f,.37f,.30f,.44f,.27f,.50f,.26f,.56f,.27f,.63f,.30f,.70f,.38f,.75f,.51f,.68f,.51f,.63f,.41f,.57f,.35f,.50f,.33f,.43f,.35f,.37f,.41f,.32f,.51f)),
            Area(2, poly(.32f,.51f,.37f,.41f,.43f,.35f,.50f,.33f,.57f,.35f,.63f,.41f,.68f,.51f,.60f,.51f,.57f,.45f,.53f,.41f,.50f,.40f,.47f,.41f,.43f,.45f,.40f,.51f)),
            Area(3, poly(.10f,.51f,.90f,.51f,.90f,.70f,.10f,.70f)),
            Area(16, poly(.10f,.70f,.90f,.70f,.90f,.74f,.10f,.74f)),
            Area(7, poly(.12f,.47f,.17f,.42f,.22f,.47f,.20f,.52f,.14f,.52f)),
            Area(7, poly(.78f,.47f,.83f,.42f,.88f,.47f,.86f,.52f,.80f,.52f)),
            Area(5, poly(.16f,.61f,.19f,.56f,.22f,.61f,.19f,.66f)),
            Area(8, poly(.78f,.62f,.81f,.57f,.84f,.62f,.81f,.67f)),
            Area(2, poly(.28f,.63f,.31f,.58f,.34f,.63f,.31f,.68f)),
            Area(1, poly(.66f,.63f,.69f,.58f,.72f,.63f,.69f,.68f)),
            Area(12, poly(.39f,.62f,.42f,.57f,.45f,.62f,.42f,.67f)),
            Area(6, poly(.55f,.62f,.58f,.57f,.61f,.62f,.58f,.67f)),
            Area(18, poly(.46f,.58f,.50f,.54f,.54f,.58f,.50f,.62f)),
            Area(3, poly(.22f,.40f,.27f,.37f,.31f,.41f,.27f,.44f)),
            Area(3, poly(.69f,.41f,.73f,.37f,.78f,.40f,.73f,.44f))
        )
    }

    private fun lionCub(): MutableList<Area> {
        fun poly(vararg pts: Float): Path = Path().apply {
            moveTo(pts[0]*width, pts[1]*height)
            var i=2
            while(i<pts.size){ lineTo(pts[i]*width, pts[i+1]*height); i+=2 }
            close()
        }
        return mutableListOf(
            Area(6, poly(.30f,.28f,.38f,.18f,.50f,.15f,.62f,.18f,.70f,.28f,.66f,.40f,.34f,.40f)),
            Area(6, poly(.34f,.40f,.66f,.40f,.70f,.52f,.64f,.66f,.50f,.72f,.36f,.66f,.30f,.52f)),
            Area(1, poly(.34f,.29f,.25f,.23f,.22f,.34f,.32f,.40f)),
            Area(1, poly(.66f,.29f,.75f,.23f,.78f,.34f,.68f,.40f)),
            Area(2, poly(.38f,.35f,.48f,.31f,.48f,.45f,.36f,.47f)),
            Area(2, poly(.52f,.31f,.62f,.35f,.64f,.47f,.52f,.45f)),
            Area(18, poly(.46f,.47f,.54f,.47f,.57f,.54f,.50f,.58f,.43f,.54f)),
            Area(8, poly(.43f,.56f,.50f,.59f,.57f,.56f,.54f,.64f,.46f,.64f)),
            Area(3, poly(.36f,.66f,.50f,.72f,.43f,.82f,.30f,.76f)),
            Area(3, poly(.50f,.72f,.64f,.66f,.70f,.76f,.57f,.82f)),
            Area(5, poly(.43f,.82f,.50f,.72f,.57f,.82f,.55f,.90f,.45f,.90f)),
            Area(4, poly(.30f,.52f,.20f,.50f,.16f,.60f,.28f,.65f)),
            Area(4, poly(.70f,.52f,.80f,.50f,.84f,.60f,.72f,.65f)),
            Area(7, poly(.28f,.65f,.18f,.68f,.22f,.78f,.34f,.73f)),
            Area(7, poly(.72f,.65f,.82f,.68f,.78f,.78f,.66f,.73f)),
            Area(10, poly(.30f,.76f,.22f,.82f,.30f,.88f,.43f,.82f)),
            Area(10, poly(.70f,.76f,.78f,.82f,.70f,.88f,.57f,.82f)),
            Area(15, poly(.38f,.18f,.32f,.12f,.28f,.22f,.30f,.28f)),
            Area(15, poly(.62f,.18f,.68f,.12f,.72f,.22f,.70f,.28f)),
            Area(12, poly(.45f,.90f,.55f,.90f,.58f,.95f,.42f,.95f))
        )
    }

    private fun dinosaur(): MutableList<Area> {
        fun poly(vararg pts: Float): Path = Path().apply {
            moveTo(pts[0]*width, pts[1]*height)
            var i=2
            while(i<pts.size){ lineTo(pts[i]*width, pts[i+1]*height); i+=2 }
            close()
        }
        return mutableListOf(
            Area(4, poly(.18f,.48f,.29f,.38f,.43f,.35f,.48f,.48f,.35f,.56f,.22f,.57f)),
            Area(5, poly(.43f,.35f,.56f,.31f,.69f,.34f,.72f,.47f,.60f,.53f,.48f,.48f)),
            Area(6, poly(.69f,.34f,.80f,.28f,.88f,.34f,.84f,.45f,.72f,.47f)),
            Area(2, poly(.80f,.28f,.84f,.20f,.91f,.23f,.88f,.34f)),
            Area(2, poly(.84f,.20f,.80f,.14f,.88f,.15f,.91f,.23f)),
            Area(7, poly(.22f,.57f,.35f,.56f,.32f,.70f,.20f,.72f)),
            Area(7, poly(.48f,.48f,.60f,.53f,.56f,.69f,.43f,.68f)),
            Area(8, poly(.20f,.72f,.32f,.70f,.30f,.83f,.18f,.84f)),
            Area(8, poly(.43f,.68f,.56f,.69f,.58f,.82f,.45f,.82f)),
            Area(3, poly(.18f,.48f,.10f,.43f,.06f,.50f,.14f,.58f,.22f,.57f)),
            Area(3, poly(.10f,.43f,.05f,.35f,.03f,.44f,.06f,.50f)),
            Area(9, poly(.29f,.38f,.32f,.27f,.39f,.35f)),
            Area(9, poly(.39f,.35f,.43f,.23f,.49f,.34f)),
            Area(9, poly(.49f,.34f,.55f,.22f,.59f,.32f)),
            Area(10, poly(.59f,.32f,.66f,.23f,.69f,.34f)),
            Area(10, poly(.69f,.34f,.75f,.25f,.80f,.28f)),
            Area(11, poly(.76f,.34f,.82f,.33f,.81f,.38f,.75f,.39f)),
            Area(12, poly(.82f,.37f,.88f,.36f,.86f,.42f,.80f,.43f)),
            Area(13, poly(.84f,.45f,.89f,.46f,.85f,.51f,.80f,.48f)),
            Area(14, poly(.32f,.70f,.38f,.68f,.39f,.76f,.31f,.77f)),
            Area(15, poly(.56f,.69f,.64f,.66f,.64f,.74f,.57f,.77f)),
            Area(16, poly(.30f,.83f,.36f,.82f,.35f,.88f,.28f,.88f))
        )
    }

    private fun mosaic(): MutableList<Area> {
        val result = mutableListOf<Area>()
        val cols = 5
        val rows = 5
        val left = width * .18f
        val top = height * .18f
        val cellW = width * .64f / cols
        val cellH = height * .48f / rows
        var n = 1
        for (r in 0 until rows) {
            for (col in 0 until cols) {
                val x = left + col * cellW
                val y = top + r * cellH
                val path = Path().apply {
                    moveTo(x, y + cellH*.12f)
                    lineTo(x + cellW*.88f, y)
                    lineTo(x + cellW, y + cellH*.82f)
                    lineTo(x + cellW*.10f, y + cellH)
                    close()
                }
                result.add(Area(n, path))
                n = if (n >= 20) 1 else n + 1
            }
        }
        return result
    }

    private fun syncAchievementHistory() {
        val done = completedDrawings()
        val total = totalCompletedPaintings()
        val creativeUnique = creativeCompletedDrawings()
        val unlocked = mutableListOf<String>()
        if (done >= 1) unlocked += "minha_primeira_arte"
        if (done >= 3) unlocked += "pequeno_artista"
        if (creativeUnique >= 3) unlocked += "artista_criativo"
        if (total >= 10) unlocked += "super_pintor"
        if (done >= drawingNames.size) unlocked += "mestre_das_cores"
        if (unlocked.isNotEmpty()) {
            val editor = prefs.edit()
            unlocked.forEach { editor.putBoolean("achievement_${it}_unlocked", true) }
            editor.apply()
        }
    }

    private fun drawAchievements(c: Canvas, w: Float, h: Float) {
        paint.style=Paint.Style.FILL; paint.color=Color.rgb(248,248,248); c.drawRect(0f,0f,w,h,paint)
        textPaint.color=Color.DKGRAY; textPaint.textSize=w*.06f; c.drawText("Minhas Estrelinhas ⭐",w/2,h*.09f,textPaint)
        val done=completedDrawings()
        data class Badge(val name:String,val target:Int,val icon:String,val usesTotal:Boolean=false)
        val creativeUnique=creativeCompletedDrawings()
        val badges=listOf(
            Badge("Minha Primeira Arte",1,"⭐"),
            Badge("Pequeno Artista",3,"🏆"),
            Badge("Artista Criativo",3,"🌈"),
            Badge("Super Pintor",10,"🎨",true),
            Badge("Mestre das Cores",drawingNames.size,"👑")
        )
        val total=totalCompletedPaintings()
        val creativeTotal=prefs.getInt("total_creative_completed", 0)
        textPaint.textSize=w*.027f; textPaint.color=Color.GRAY
        c.drawText("$done artes diferentes • $total pinturas prontas",w/2,h*.145f,textPaint)
        textPaint.textSize=w*.024f
        c.drawText("🎨 $creativeUnique artes criativas • $creativeTotal conclusões",w/2,h*.172f,textPaint)
        badges.forEachIndexed { i,b ->
            val progress=when {
                b.name == "Artista Criativo" -> creativeUnique
                b.usesTotal -> total
                else -> done
            }
            val unlocked=progress>=b.target
            val cy=h*(.225f+i*.125f)
            paint.color=if(unlocked) Color.rgb(255,244,200) else Color.rgb(232,232,232)
            c.drawRoundRect(w*.12f,cy-h*.055f,w*.88f,cy+h*.055f,28f,28f,paint)
            textPaint.textSize=w*.050f; textPaint.color=if(unlocked) Color.rgb(170,120,20) else Color.GRAY
            c.drawText(b.icon,w*.23f,cy+h*.016f,textPaint)
            textPaint.textSize=w*.032f; c.drawText(b.name,w*.57f,cy-h*.004f,textPaint)
            textPaint.textSize=w*.024f
            val unit=when {
                b.name == "Artista Criativo" -> "artes criativas"
                b.usesTotal -> "pinturas"
                else -> "artes"
            }
            c.drawText(if(unlocked) "Conquistado ✓" else "$progress/${b.target} $unit",w*.57f,cy+h*.032f,textPaint)
        }
        paint.color=Color.rgb(225,235,255); c.drawRoundRect(w*.28f,h*.86f,w*.72f,h*.93f,24f,24f,paint)
        textPaint.textSize=w*.032f; textPaint.color=Color.DKGRAY; c.drawText("← Voltar à galeria",w/2,h*.905f,textPaint)
    }

    private fun wasEverCompleted(index: Int): Boolean =
        prefs.getBoolean("drawing_${index}_ever_completed", false) ||
            prefs.getBoolean("drawing_${index}_numbers_complete", false)

    private fun visibleDrawingIndices(): List<Int> =
        drawingNames.indices.filter { i ->
            galleryCategory == 0 ||
                (galleryCategory == 1 && i <= 5) ||
                (galleryCategory == 2 && i >= 6) ||
                (galleryCategory == 3 && wasEverCompleted(i)) ||
                (galleryCategory == 4 && (prefs.getBoolean("drawing_${i}_creative_ever_completed", false) || savedCreativeProgress(i) > 0))
        }

    private fun drawGallery(c: Canvas, w: Float, h: Float) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(248,248,248)
        c.drawRect(0f, 0f, w, h, paint)

        textPaint.color = Color.DKGRAY
        textPaint.textSize = w * .065f
        c.drawText("Minhas Artes", w/2, h*.075f, textPaint)
        textPaint.textSize = w * .032f
        textPaint.color = Color.GRAY
        c.drawText("Escolha, pinte e divirta-se! 🎨", w/2, h*.115f, textPaint)
        textPaint.textSize = w*.028f
        textPaint.color = Color.rgb(90,90,90)
        c.drawText("🎟 Tickets: ${ticketCount()}", w*.82f, h*.075f, textPaint)
        paint.color=Color.rgb(255,244,210); c.drawRoundRect(w*.03f,h*.055f,w*.25f,h*.10f,16f,16f,paint)
        textPaint.textSize=w*.024f; textPaint.color=Color.DKGRAY; c.drawText("🏆 ${completedDrawings()}",w*.14f,h*.084f,textPaint)

        val tabY = h*.145f
        val tabGap = w*.19f
        for (i in galleryCategories.indices) {
            val x = w*.12f + i*tabGap
            paint.style=Paint.Style.FILL
            paint.color=if(i==galleryCategory) Color.rgb(220,232,255) else Color.rgb(238,238,238)
            c.drawRoundRect(x-w*.09f,tabY-h*.025f,x+w*.09f,tabY+h*.025f,18f,18f,paint)
            textPaint.textSize=w*.022f; textPaint.color=Color.DKGRAY
            c.drawText(galleryCategories[i],x,tabY+h*.008f,textPaint)
        }
        val visibleIndices = visibleDrawingIndices()
        val cardW = w*.40f
        val cardH = h*.20f
        val lefts = listOf(w*.07f, w*.53f)
        for ((position, i) in visibleIndices.withIndex()) {
            val row = position / 2
            val col = position % 2
            val l = lefts[col]
            val t = h*.19f + row*h*.235f - galleryScroll
            paint.color = Color.WHITE
            paint.setShadowLayer(8f, 0f, 3f, Color.LTGRAY)
            setLayerType(LAYER_TYPE_SOFTWARE, paint)
            c.drawRoundRect(l,t,l+cardW,t+cardH,24f,24f,paint)
            paint.clearShadowLayer()

            // Simple recognizable thumbnails instead of placeholder circles.
            val cx = l + cardW/2
            val cy = t + cardH*.36f
            paint.style = Paint.Style.FILL
            when (i) {
                0 -> { // butterfly
                    paint.color=colors[4]; c.drawOval(cx-cardW*.23f,cy-cardH*.18f,cx-cardW*.03f,cy+cardH*.13f,paint); c.drawOval(cx+cardW*.03f,cy-cardH*.18f,cx+cardW*.23f,cy+cardH*.13f,paint)
                    paint.color=colors[5]; c.drawRoundRect(cx-cardW*.025f,cy-cardH*.18f,cx+cardW*.025f,cy+cardH*.18f,12f,12f,paint)
                }
                1 -> { // fish
                    paint.color=colors[3]; c.drawOval(cx-cardW*.22f,cy-cardH*.12f,cx+cardW*.14f,cy+cardH*.12f,paint)
                    paint.color=colors[1]; val tail=Path(); tail.moveTo(cx+cardW*.10f,cy); tail.lineTo(cx+cardW*.27f,cy-cardH*.13f); tail.lineTo(cx+cardW*.27f,cy+cardH*.13f); tail.close(); c.drawPath(tail,paint)
                }
                2 -> { // turtle
                    paint.color=colors[2]; c.drawOval(cx-cardW*.18f,cy-cardH*.13f,cx+cardW*.16f,cy+cardH*.13f,paint); c.drawCircle(cx+cardW*.22f,cy,cardW*.07f,paint)
                }
                3 -> { // rocket
                    paint.color=colors[3]; val rocket=Path(); rocket.moveTo(cx,cy-cardH*.22f); rocket.lineTo(cx-cardW*.10f,cy+cardH*.13f); rocket.lineTo(cx+cardW*.10f,cy+cardH*.13f); rocket.close(); c.drawPath(rocket,paint)
                    paint.color=colors[0]; val flame=Path(); flame.moveTo(cx-cardW*.05f,cy+cardH*.12f); flame.lineTo(cx,cy+cardH*.25f); flame.lineTo(cx+cardW*.05f,cy+cardH*.12f); flame.close(); c.drawPath(flame,paint)
                }
                4 -> { // flower
                    paint.color=colors[1]; for (a in 0 until 6) { val angle=Math.toRadians((a*60).toDouble()); val px=cx+kotlin.math.cos(angle).toFloat()*cardW*.11f; val py=cy+kotlin.math.sin(angle).toFloat()*cardH*.10f; c.drawCircle(px,py,cardW*.075f,paint) }
                    paint.color=colors[5]; c.drawCircle(cx,cy,cardW*.07f,paint)
                }
                5 -> { // ice cream
                    paint.color=colors[7]; c.drawCircle(cx,cy-cardH*.07f,cardW*.13f,paint)
                    paint.color=Color.rgb(205,150,90); val cone=Path(); cone.moveTo(cx-cardW*.11f,cy); cone.lineTo(cx+cardW*.11f,cy); cone.lineTo(cx,cy+cardH*.24f); cone.close(); c.drawPath(cone,paint)
                }
                6 -> { // mosaic
                    for (rr in 0 until 3) for (cc in 0 until 4) {
                        paint.color=colors[(rr*4+cc)%colors.size]
                        val x=cx-cardW*.22f+cc*cardW*.11f; val y=cy-cardH*.18f+rr*cardH*.12f
                        c.drawRect(x,y,x+cardW*.095f,y+cardH*.10f,paint)
                    }
                }
                7 -> { // landscape
                    paint.color=colors[3]; c.drawRect(cx-cardW*.25f,cy-cardH*.20f,cx+cardW*.25f,cy,paint)
                    paint.color=colors[2]; c.drawRect(cx-cardW*.25f,cy,cx+cardW*.25f,cy+cardH*.18f,paint)
                    paint.color=colors[9]; val mountain=Path(); mountain.moveTo(cx-cardW*.23f,cy); mountain.lineTo(cx-cardW*.07f,cy-cardH*.15f); mountain.lineTo(cx+cardW*.05f,cy); mountain.lineTo(cx+cardW*.16f,cy-cardH*.12f); mountain.lineTo(cx+cardW*.25f,cy); mountain.close(); c.drawPath(mountain,paint)
                }
                8 -> { // rainbow
                    val oldStyle=paint.style; val oldWidth=paint.strokeWidth; val oldCap=paint.strokeCap; val oldColor=paint.color
                    paint.style=Paint.Style.STROKE; paint.strokeCap=Paint.Cap.ROUND
                    val bands=listOf(colors[0],colors[5],colors[1],colors[2],colors[3])
                    bands.forEachIndexed { band,color ->
                        paint.color=color
                        paint.strokeWidth=cardW*.035f
                        val inset=band*cardW*.035f
                        val oval=RectF(cx-cardW*.23f+inset,cy-cardH*.03f+inset,cx+cardW*.23f-inset,cy+cardH*.32f)
                        c.drawArc(oval,180f,180f,false,paint)
                    }
                    paint.style=oldStyle; paint.strokeWidth=oldWidth; paint.strokeCap=oldCap; paint.color=oldColor
                }
                9 -> { // lion cub
                    paint.color=colors[6]; c.drawCircle(cx,cy,cardW*.22f,paint)
                    paint.color=colors[5]; c.drawCircle(cx,cy+cardH*.01f,cardW*.16f,paint)
                    paint.color=colors[5]; c.drawCircle(cx-cardW*.15f,cy-cardH*.13f,cardW*.07f,paint); c.drawCircle(cx+cardW*.15f,cy-cardH*.13f,cardW*.07f,paint)
                    paint.color=Color.DKGRAY; c.drawCircle(cx-cardW*.055f,cy-cardH*.025f,cardW*.018f,paint); c.drawCircle(cx+cardW*.055f,cy-cardH*.025f,cardW*.018f,paint)
                    paint.color=colors[8]; val nose=Path(); nose.moveTo(cx,cy+cardH*.035f); nose.lineTo(cx-cardW*.025f,cy+cardH*.07f); nose.lineTo(cx+cardW*.025f,cy+cardH*.07f); nose.close(); c.drawPath(nose,paint)
                }
                else -> { // dinosaur
                    paint.color=colors[2]
                    c.drawOval(cx-cardW*.22f,cy-cardH*.08f,cx+cardW*.13f,cy+cardH*.14f,paint)
                    c.drawCircle(cx+cardW*.19f,cy-cardH*.07f,cardW*.09f,paint)
                    val tail=Path(); tail.moveTo(cx-cardW*.18f,cy); tail.lineTo(cx-cardW*.31f,cy-cardH*.08f); tail.lineTo(cx-cardW*.24f,cy+cardH*.08f); tail.close(); c.drawPath(tail,paint)
                    paint.color=colors[4]
                    for (spike in 0 until 4) {
                        val x=cx-cardW*.12f+spike*cardW*.08f
                        val crest=Path(); crest.moveTo(x,cy-cardH*.08f); crest.lineTo(x+cardW*.04f,cy-cardH*.19f); crest.lineTo(x+cardW*.08f,cy-cardH*.07f); crest.close(); c.drawPath(crest,paint)
                    }
                    paint.color=Color.DKGRAY; c.drawCircle(cx+cardW*.21f,cy-cardH*.09f,cardW*.014f,paint)
                    paint.color=colors[2]; c.drawRect(cx-cardW*.11f,cy+cardH*.10f,cx-cardW*.05f,cy+cardH*.23f,paint); c.drawRect(cx+cardW*.04f,cy+cardH*.10f,cx+cardW*.10f,cy+cardH*.23f,paint)
                }
            }

            textPaint.textSize=w*.036f
            textPaint.color=Color.DKGRAY
            c.drawText(drawingNames[i],l+cardW/2,t+cardH*.72f,textPaint)
            val progress = savedProgress(i)
            val creativeProgress = savedCreativeProgress(i)
            textPaint.textSize=w*.027f
            textPaint.color=if (progress == 100 || creativeProgress == 100) Color.rgb(60,150,80) else Color.GRAY
            val locked = i >= unlockedDrawingCount()
            val status = when {
                locked -> "🔒 Assistir para liberar"
                progress == 100 && creativeProgress == 100 -> "⭐ Números + Criativo"
                progress == 100 -> "Concluído ✓"
                creativeProgress == 100 -> "🎨 Criativo concluído"
                creativeProgress > 0 -> "$progress% • 🎨 $creativeProgress%"
                else -> "$progress%"
            }
            c.drawText(status,l+cardW/2,t+cardH*.88f,textPaint)
        }

        paint.color=Color.rgb(235,242,255)
        c.drawRoundRect(w*.12f,h*.88f,w*.88f,h*.95f,24f,24f,paint)
        textPaint.textSize=w*.035f; textPaint.color=Color.DKGRAY
        c.drawText(if (unlockedDrawingCount() < drawingNames.size) {
            if (ticketCount() > 0) "🎟 Usar ticket • liberar 2 artes" else "▶ Assistir vídeo • ganhar 1 ticket"
        } else "📷 Câmera • em breve",w/2,h*.925f,textPaint)
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val w = width.toFloat(); val h = height.toFloat()
        if (achievementsMode) { drawAchievements(c,w,h); return }
        if (galleryMode) {
            drawGallery(c, w, h)
            return
        }
        paint.style = Paint.Style.FILL; paint.color = Color.rgb(248,248,248)
        c.drawRect(0f,0f,w,h*.11f,paint)
        textPaint.textSize = min(w,h)*.045f
        textPaint.color = Color.DKGRAY
        c.drawText(drawingNames[drawingIndex], w/2,h*.07f,textPaint)

        c.save()
        c.translate(offsetX, offsetY)
        c.scale(scaleFactor, scaleFactor, w/2, h*.43f)

        areas.forEach { a ->
            paint.style = Paint.Style.FILL
            paint.color = when {
                a.painted -> if (creativeMode) (a.creativeColor ?: colors[a.number - 1]) else colors[a.number-1]
                a === wrongArea -> Color.rgb(255, 225, 225)
                a.number == selected -> Color.rgb(248, 248, 248)
                else -> Color.WHITE
            }
            c.drawPath(a.path,paint)

            paint.style = Paint.Style.STROKE
            paint.strokeWidth = when {
                a === wrongArea -> 6f
                !a.painted && a.number == selected -> 5f
                else -> 3f
            }
            paint.color = when {
                a === wrongArea -> Color.rgb(220, 60, 60)
                !a.painted && a.number == selected -> colors[a.number - 1]
                else -> Color.rgb(120,120,120)
            }
            c.drawPath(a.path,paint)
            if(!a.painted && !creativeMode){
                val b=RectF(); a.path.computeBounds(b,true)
                textPaint.textSize=min(w,h)*.034f; textPaint.color=Color.rgb(110,110,110)
                c.drawText(a.number.toString(),b.centerX(),b.centerY()+textPaint.textSize*.35f,textPaint)
            }
        }

        // Mode switch
        paint.style=Paint.Style.FILL
        paint.color=if (!creativeMode) Color.rgb(225,235,255) else Color.rgb(245,245,245)
        c.drawRoundRect(w*.18f,h*.755f,w*.49f,h*.795f,18f,18f,paint)
        paint.color=if (creativeMode) Color.rgb(255,235,220) else Color.rgb(245,245,245)
        c.drawRoundRect(w*.51f,h*.755f,w*.82f,h*.795f,18f,18f,paint)
        textPaint.textSize=w*.028f; textPaint.color=Color.DKGRAY
        c.drawText("Por números",w*.335f,h*.782f,textPaint)
        c.drawText("Criativo",w*.665f,h*.782f,textPaint)

        c.restore()

        val y=h*.84f
        val visibleCount = 8
        // Number mode: once every region of a color is painted, remove that
        // color from the palette. Creative mode keeps the full palette.
        val paletteNumbers = if (creativeMode) {
            colors.indices.map { it + 1 }
        } else {
            areas.map { it.number }.distinct().sorted().filterNot { isNumberComplete(it) }
        }
        val selectedPos = paletteNumbers.indexOf(selected).coerceAtLeast(0)
        val paletteStart = (selectedPos - visibleCount / 2).coerceIn(0, (paletteNumbers.size - visibleCount).coerceAtLeast(0))
        val visibleNumbers = paletteNumbers.drop(paletteStart).take(visibleCount)
        for ((slot, number) in visibleNumbers.withIndex()) {
            val i = number - 1
            val col = colors[i]
            val complete = false
            val x=w*(.075f+slot*.122f)

            paint.style=Paint.Style.FILL
            paint.color=if (complete) Color.rgb(225,225,225) else col
            c.drawCircle(x,y,w*.045f,paint)

            paint.style=Paint.Style.STROKE
            paint.strokeWidth=if(selected==number && !complete) 8f else 2f
            paint.color=if(selected==number && !complete) Color.BLACK else Color.LTGRAY
            c.drawCircle(x,y,w*.052f,paint)

            textPaint.textSize=w*.032f
            textPaint.color=if (complete) Color.DKGRAY else Color.WHITE
            c.drawText(if (complete) "✓" else if (creativeMode) "●" else number.toString(),x,y+textPaint.textSize*.35f,textPaint)
        }
        if (creativeMode) {
            textPaint.textSize=w*.025f; textPaint.color=Color.DKGRAY
            c.drawText(if (specialColorsUnlocked()) "✨ Especiais liberadas" else "✨ Especiais • 1 ticket",w/2,h*.91f,textPaint)
            if (specialColorsUnlocked()) {
                for (i in specialColors.indices) {
                    val x=w*(.38f+i*.12f)
                    paint.style=Paint.Style.FILL; paint.color=specialColors[i]; c.drawCircle(x,h*.95f,w*.035f,paint)
                    paint.style=Paint.Style.STROKE; paint.strokeWidth=if(selectedSpecial==i) 7f else 2f; paint.color=Color.DKGRAY; c.drawCircle(x,h*.95f,w*.041f,paint)
                }
            }
        }

        if (!creativeMode && hintUntil > System.currentTimeMillis()) {
            areas.filter { !it.painted && it.number == selected }.forEach { a ->
                paint.style=Paint.Style.STROKE; paint.strokeWidth=10f
                paint.color=Color.argb(220,255,193,7); c.drawPath(a.path,paint)
            }
            postInvalidateDelayed(100)
        }

        if (!creativeMode && isDrawingComplete()) {
            textPaint.textSize = w * .055f
            textPaint.color = Color.rgb(60, 150, 80)
            c.drawText("Concluído! ✓", w/2, h*.77f, textPaint)
        }

        if (completionCardVisible && isDrawingComplete()) {
            paint.style=Paint.Style.FILL; paint.color=Color.rgb(230,245,255)
            c.drawRoundRect(w*.12f,h*.62f,w*.49f,h*.70f,22f,22f,paint)
            paint.color=Color.rgb(255,244,210)
            c.drawRoundRect(w*.51f,h*.62f,w*.88f,h*.70f,22f,22f,paint)
            textPaint.textSize=w*.027f; textPaint.color=Color.DKGRAY
            c.drawText("📤 Compartilhar",w*.305f,h*.67f,textPaint)
            c.drawText("🎨 Pintar de novo",w*.695f,h*.67f,textPaint)
        }

        if (rewardUntil > System.currentTimeMillis()) {
            celebrationParticles.forEachIndexed { i,p ->
                paint.style=Paint.Style.FILL
                paint.color=colors[i % colors.size]
                val px=w*p.first; val py=h*p.second
                if(i%2==0) c.drawCircle(px,py,w*.012f,paint) else c.drawRect(px-w*.009f,py-w*.009f,px+w*.009f,py+w*.009f,paint)
            }
            paint.style = Paint.Style.FILL
            paint.color = Color.argb(225, 255, 248, 220)
            c.drawRoundRect(w*.12f,h*.30f,w*.88f,h*.58f,36f,36f,paint)
            textPaint.color = Color.rgb(180,130,20)
            textPaint.textSize = w*.11f
            c.drawText("🏆",w/2,h*.39f,textPaint)
            textPaint.textSize = w*.052f
            c.drawText(rewardTitle,w/2,h*.47f,textPaint)
            textPaint.textSize = w*.032f
            textPaint.color = Color.DKGRAY
            c.drawText("Pintura pronta! Você ganhou uma estrelinha ⭐",w/2,h*.53f,textPaint)
            postInvalidateDelayed(80)
        }

        paint.style=Paint.Style.FILL; paint.color=Color.rgb(245,245,245)
        c.drawRoundRect(w*.08f,h*.91f,w*.28f,h*.97f,20f,20f,paint)
        c.drawRoundRect(w*.36f,h*.91f,w*.64f,h*.97f,20f,20f,paint)
        c.drawRoundRect(w*.72f,h*.91f,w*.92f,h*.97f,20f,20f,paint)
        textPaint.textSize=w*.033f; textPaint.color=Color.DKGRAY
        c.drawText("‹ Anterior",w*.18f,h*.95f,textPaint)
        c.drawText("Galeria",w*.50f,h*.95f,textPaint)
        c.drawText("Próximo ›",w*.82f,h*.95f,textPaint)
        if (!creativeMode && !isDrawingComplete()) {
            paint.color=Color.rgb(255,244,210); c.drawRoundRect(w*.38f,h*.855f,w*.62f,h*.90f,16f,16f,paint)
            textPaint.textSize=w*.026f; textPaint.color=Color.DKGRAY; c.drawText("💡 Dica",w*.50f,h*.885f,textPaint)
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        val w=width.toFloat(); val h=height.toFloat()

        if (!galleryMode && e.pointerCount == 2) {
            val dx = e.getX(0) - e.getX(1)
            val dy = e.getY(0) - e.getY(1)
            val distance = kotlin.math.sqrt(dx*dx + dy*dy)
            when (e.actionMasked) {
                MotionEvent.ACTION_POINTER_DOWN -> lastTouchX = distance
                MotionEvent.ACTION_MOVE -> {
                    if (lastTouchX > 0f) {
                        scaleFactor = (scaleFactor * (distance / lastTouchX)).coerceIn(1f, 4f)
                        clampPan()
                        invalidate()
                    }
                    lastTouchX = distance
                }
                MotionEvent.ACTION_POINTER_UP -> lastTouchX = 0f
            }
            return true
        }

        if (!galleryMode && scaleFactor > 1f && e.y < h*.74f) {
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = e.x; lastTouchY = e.y; dragging = false
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = e.x-lastTouchX; val dy=e.y-lastTouchY
                    if (kotlin.math.abs(dx)+kotlin.math.abs(dy) > 5f) dragging=true
                    offsetX += dx; offsetY += dy
                    clampPan()
                    lastTouchX=e.x; lastTouchY=e.y
                    invalidate(); return true
                }
                MotionEvent.ACTION_UP -> if (dragging) { dragging=false; return true }
            }
        }

        if(e.action!=MotionEvent.ACTION_UP) return true

        if (achievementsMode) {
            if (e.action == MotionEvent.ACTION_UP && e.y in h*.84f..h*.96f) { achievementsMode=false; galleryMode=true; invalidate() }
            return true
        }
        if (galleryMode) {
            if (e.action == MotionEvent.ACTION_DOWN) {
                galleryDownY = e.y
                galleryStartScroll = galleryScroll
                return true
            }
            if (e.action == MotionEvent.ACTION_MOVE) {
                val filteredCount = visibleDrawingIndices().size
                val maxScroll = (((filteredCount + 1) / 2) * h*.235f - h*.68f).coerceAtLeast(0f)
                galleryScroll = (galleryStartScroll + galleryDownY - e.y).coerceIn(0f, maxScroll)
                invalidate()
                return true
            }
            if (e.action != MotionEvent.ACTION_UP) return true
            if (kotlin.math.abs(e.y - galleryDownY) > 18f) return true
            if (e.y in h*.04f..h*.11f && e.x < w*.30f) {
                achievementsMode=true; galleryMode=false; invalidate(); return true
            }
            if (e.y in h*.12f..h*.18f) {
                // Match the visual center of each gallery tab instead of dividing
                // the whole row into equal buckets. This keeps edge taps accurate.
                val tabGap = w*.19f
                val firstCenter = w*.12f
                val nearestTab = galleryCategories.indices.minByOrNull { i ->
                    kotlin.math.abs(e.x - (firstCenter + i*tabGap))
                } ?: 0
                galleryCategory = nearestTab
                galleryScroll = 0f
                invalidate()
                return true
            }
            if (e.y in h*.88f..h*.95f && unlockedDrawingCount() < drawingNames.size) {
                if (!spendTicketToUnlock()) earnRewardTicket() // simulated rewarded video
                return true
            }
            if (e.y in h*.14f..h*.87f) {
                val row = ((e.y + galleryScroll - h*.19f) / (h*.235f)).toInt()
                val col = if (e.x < w/2) 0 else 1
                val visibleIndices = visibleDrawingIndices()
                val position = row*2 + col
                val index = visibleIndices.getOrNull(position) ?: -1
                if (index in drawingNames.indices) {
                    if (index >= unlockedDrawingCount()) {
                        rewardTitle = "Arte bloqueada • assista para liberar"
                        rewardUntil = System.currentTimeMillis() + 1600L
                        invalidate()
                        return true
                    }
                    drawingIndex = index
                    galleryMode = false
                    creativeMode = false
                    rebuild()
                }
            }
            return true
        }
        if (!creativeMode && isDrawingComplete() && completionCardVisible && e.y in h*.60f..h*.72f) {
            when {
                e.x in w*.10f..w*.50f -> shareArtwork()
                e.x in w*.50f..w*.90f -> repaintCurrentDrawing()
            }
            return true
        }
        if (!creativeMode && !isDrawingComplete() && e.y in h*.845f..h*.905f && e.x in w*.34f..w*.66f) {
            hintUntil = System.currentTimeMillis() + 1800L
            invalidate()
            return true
        }
        if(e.y in h*.745f..h*.805f){
            val requestedCreativeMode = e.x >= w*.50f
            if (requestedCreativeMode != creativeMode) {
                saveProgress()
                creativeMode = requestedCreativeMode
                areas.forEach { area ->
                    area.painted = false
                    area.creativeColor = null
                }
                restoreProgress()
                completionCardVisible = isDrawingComplete()
                completionRewardShown = isDrawingComplete()
                selected = nextIncompleteNumber() ?: (areas.firstOrNull()?.number ?: 1)
                selectedSpecial = -1
            }
            wrongArea = null
            invalidate()
            return true
        }
        if (creativeMode && e.y in h*.89f..h*.98f) {
            if (!specialColorsUnlocked()) {
                if (!unlockSpecialColors()) {
                    rewardTitle = "Você precisa de 1 ticket 🎟"
                    rewardUntil = System.currentTimeMillis() + 1600L
                    invalidate()
                }
            } else {
                val candidates = specialColors.indices.minByOrNull { kotlin.math.abs(e.x - w*(.38f+it*.12f)) } ?: 0
                selectedSpecial = candidates
                wrongArea = null
                invalidate()
            }
            return true
        }
        if(e.y in h*.79f..h*.89f){
            val paletteNumbers = if (creativeMode) {
                colors.indices.map { it + 1 }
            } else {
                areas.map { it.number }.distinct().sorted().filterNot { isNumberComplete(it) }
            }
            if (paletteNumbers.isNotEmpty()) {
                val selectedPos = paletteNumbers.indexOf(selected).coerceAtLeast(0)
                val start = (selectedPos - 4).coerceIn(0, (paletteNumbers.size - 8).coerceAtLeast(0))
                val visibleNumbers = paletteNumbers.drop(start).take(8)
                val slot = visibleNumbers.indices.minByOrNull { i ->
                    kotlin.math.abs(e.x - w*(.075f+i*.122f))
                } ?: 0
                visibleNumbers.getOrNull(slot)?.let { number ->
                    selected = number
                    selectedSpecial = -1
                    wrongArea = null
                    invalidate()
                }
            }
            return true
        }
        if(e.y>h*.90f){
            if (e.x in w*.36f..w*.64f) {
                galleryMode = true
                invalidate()
            } else if (e.x < w*.32f) {
                drawingIndex = (drawingIndex - 1 + drawingNames.size) % drawingNames.size
                rebuild()
            } else if (e.x > w*.68f) {
                drawingIndex = (drawingIndex + 1) % drawingNames.size
                rebuild()
            }
            return true
        }
        val pivotX = w/2
        val pivotY = h*.43f
        val touchX = (e.x - offsetX - pivotX) / scaleFactor + pivotX
        val touchY = (e.y - offsetY - pivotY) / scaleFactor + pivotY

        if (creativeMode) {
            val touched = areas.lastOrNull { contains(it.path, touchX, touchY) }
            if (touched != null) {
                val wasComplete = isDrawingComplete()
                touched.painted = true
                touched.creativeColor = if (selectedSpecial in specialColors.indices) {
                    specialColors[selectedSpecial]
                } else {
                    colors[selected - 1]
                }
                saveProgress()
                if (!wasComplete && isDrawingComplete()) {
                    completionCardVisible = true
                    val creativeCelebratedKey = "achievement_artista_criativo_celebrated"
                    val hadCreativeCelebration = prefs.getBoolean(creativeCelebratedKey, false)
                    recordCreativeCompletionOnce()
                    val unlockedCreativeBadge = creativeCompletedDrawings() >= 3 && !hadCreativeCelebration
                    if (unlockedCreativeBadge) {
                        prefs.edit().putBoolean(creativeCelebratedKey, true).apply()
                        rewardTitle = "Artista Criativo! 🌈"
                        rewardUntil = System.currentTimeMillis() + 2200L
                    } else {
                        rewardTitle = "Modo criativo completo! ✨"
                        rewardUntil = System.currentTimeMillis() + 1800L
                    }
                    celebrationParticles = List(28) { i ->
                        Pair(((i * 37) % 100) / 100f, ((i * 61) % 70) / 100f + .12f)
                    }
                }
                invalidate()
            }
            return true
        }

        // Prefer the currently selected numbered region. Some drawings have
        // intentionally overlapping paths (for example, the fish eye sits
        // inside the body), so checking the first path alone can block it.
        val correct = areas.firstOrNull {
            it.number == selected && !it.painted && contains(it.path, touchX, touchY)
        }
        if (correct != null) {
            val wasComplete = isDrawingComplete()
            correct.painted = true
            wrongArea = null
            saveProgress()
            if (!wasComplete && isDrawingComplete()) triggerCompletionReward()
            if (isNumberComplete(selected)) {
                nextIncompleteNumber()?.let { selected = it }
            }
            invalidate()
            return true
        }

        val touchedWrong = areas.firstOrNull {
            !it.painted && contains(it.path, touchX, touchY)
        }
        if (touchedWrong != null) {
            wrongArea = touchedWrong
            invalidate()
            postDelayed({
                if (wrongArea === touchedWrong) {
                    wrongArea = null
                    invalidate()
                }
            }, 280)
        }
        return true
    }

    private fun contains(path: Path,x:Float,y:Float):Boolean{
        val b=RectF(); path.computeBounds(b,true)
        val clip=Region(b.left.toInt(),b.top.toInt(),b.right.toInt(),b.bottom.toInt())
        return Region().apply{ setPath(path,clip) }.contains(x.toInt(),y.toInt())
    }
}
