package com.pedroxs11.bichinhosfazendinha.ui

import android.content.SharedPreferences
import android.os.SystemClock
import com.pedroxs11.bichinhosfazendinha.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val KEY_TOTAL_STARS = "stars"
private const val KEY_DAILY_STARS = "daily_stars"
private const val KEY_DAILY_DATE = "daily_stars_date"
private const val KEY_UNLOCK_PREFIX = "animal_unlocked_"
private const val REWARD_DEBOUNCE_MS = 750L

const val DAILY_STAR_LIMIT = 25

val ANIMAL_UNLOCK_COSTS = linkedMapOf(
    "chick" to 0,
    "rabbit" to 0,
    "dog" to 175,
    "pig" to 250,
    "duck" to 350,
    "sheep" to 500,
    "goat" to 700,
    "cow" to 900,
    "horse" to 1200,
    "donkey" to 1500
)

data class StarRewardResult(
    val requested: Int,
    val granted: Int,
    val totalStars: Int,
    val dailyStars: Int,
    val dailyLimit: Int = DAILY_STAR_LIMIT
) {
    val dailyLimitReached: Boolean
        get() = dailyStars >= dailyLimit
}

class GameProgression(private val prefs: SharedPreferences) {

    private var lastRewardAtMs = 0L
    private var lastRewardDate: String? = null
    private var lastRewardResult: StarRewardResult? = null

    private fun safeInt(key: String, default: Int = 0): Int {
        val raw = prefs.all[key]
        val value = when (raw) {
            is Int -> raw
            is Long -> raw.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
            is Float -> raw.toInt()
            is String -> raw.toIntOrNull() ?: default
            else -> default
        }
        if (raw != null && raw !is Int) {
            prefs.edit().putInt(key, value).apply()
        }
        return value
    }

    private fun safeBoolean(key: String, default: Boolean = false): Boolean {
        val raw = prefs.all[key]
        val value = when (raw) {
            is Boolean -> raw
            is Int -> raw != 0
            is Long -> raw != 0L
            is String -> raw.equals("true", ignoreCase = true) || raw == "1"
            else -> default
        }
        if (raw != null && raw !is Boolean) {
            prefs.edit().putBoolean(key, value).apply()
        }
        return value
    }

    private fun safeDate(key: String): String? {
        val raw = prefs.all[key] ?: return null
        val value = when (raw) {
            is String -> raw
            is Int -> raw.toString()
            is Long -> raw.toString()
            else -> null
        }
        if (value != null && raw !is String) {
            prefs.edit().putString(key, value).apply()
        }
        return value
    }

    fun totalStars(): Int {
        val saved = safeInt(KEY_TOTAL_STARS)
        val safe = saved.coerceAtLeast(0)
        if (safe != saved) {
            prefs.edit().putInt(KEY_TOTAL_STARS, safe).apply()
        }
        return safe
    }

    fun dailyStars(): Int {
        val today = todayKey()
        resetDailyCounterIfNeeded(today)
        return sanitizedDailyStars()
    }

    fun rewardStars(amount: Int): StarRewardResult {
        val rewardDate = todayKey()
        resetDailyCounterIfNeeded(rewardDate)

        val requested = amount.coerceAtLeast(0)
        val now = SystemClock.elapsedRealtime()
        val cached = lastRewardResult
        if (
            cached != null &&
            lastRewardDate == rewardDate &&
            cached.requested == requested &&
            now - lastRewardAtMs in 0 until REWARD_DEBOUNCE_MS
        ) {
            return cached
        }

        val currentDaily = sanitizedDailyStars()
        val currentTotal = totalStars()
        val remainingToday = (DAILY_STAR_LIMIT - currentDaily).coerceAtLeast(0)
        val remainingTotalCapacity = (Int.MAX_VALUE.toLong() - currentTotal.toLong())
            .coerceAtLeast(0L)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
        val granted = requested
            .coerceAtMost(remainingToday)
            .coerceAtMost(remainingTotalCapacity)
        val newDaily = currentDaily + granted
        val newTotal = currentTotal + granted

        if (granted > 0 || safeDate(KEY_DAILY_DATE) != rewardDate) {
            prefs.edit()
                .putInt(KEY_DAILY_STARS, newDaily)
                .putInt(KEY_TOTAL_STARS, newTotal)
                .putString(KEY_DAILY_DATE, rewardDate)
                .apply()
        }

        val result = StarRewardResult(
            requested = requested,
            granted = granted,
            totalStars = newTotal,
            dailyStars = newDaily
        )
        lastRewardAtMs = now
        lastRewardDate = rewardDate
        lastRewardResult = result
        return result
    }

    fun unlockCost(animalId: String): Int = ANIMAL_UNLOCK_COSTS[animalId] ?: 0

    fun isUnlocked(animalId: String, startsUnlocked: Boolean = unlockCost(animalId) == 0): Boolean {
        if (!ANIMAL_UNLOCK_COSTS.containsKey(animalId)) return false
        if (BuildConfig.DEBUG) return true
        val canonicallyStartsUnlocked = unlockCost(animalId) == 0
        return canonicallyStartsUnlocked || safeBoolean(KEY_UNLOCK_PREFIX + animalId)
    }

    fun canUnlock(animalId: String): Boolean {
        val ids = ANIMAL_UNLOCK_COSTS.keys.toList()
        val index = ids.indexOf(animalId)
        if (index < 0) return false
        if (BuildConfig.DEBUG) return true
        if (index == 0 || unlockCost(animalId) <= 0) return true

        val previousId = ids[index - 1]
        return isUnlocked(previousId)
    }

    fun unlock(animalId: String, cost: Int = unlockCost(animalId)): Boolean {
        if (!ANIMAL_UNLOCK_COSTS.containsKey(animalId)) return false
        if (isUnlocked(animalId)) return true
        if (!canUnlock(animalId)) return false

        val canonicalCost = unlockCost(animalId)
        if (cost != canonicalCost) return false

        if (canonicalCost <= 0) {
            prefs.edit().putBoolean(KEY_UNLOCK_PREFIX + animalId, true).apply()
            return true
        }

        val currentTotal = totalStars()
        if (currentTotal < canonicalCost) return false

        prefs.edit()
            .putInt(KEY_TOTAL_STARS, currentTotal - canonicalCost)
            .putBoolean(KEY_UNLOCK_PREFIX + animalId, true)
            .apply()
        return true
    }

    fun starsMissingFor(cost: Int): Int = (cost - totalStars()).coerceAtLeast(0)

    fun starsMissingForAnimal(animalId: String): Int {
        if (!ANIMAL_UNLOCK_COSTS.containsKey(animalId)) return Int.MAX_VALUE
        return starsMissingFor(unlockCost(animalId))
    }

    private fun sanitizedDailyStars(): Int {
        val saved = safeInt(KEY_DAILY_STARS)
        val safe = saved.coerceIn(0, DAILY_STAR_LIMIT)
        if (safe != saved) {
            prefs.edit().putInt(KEY_DAILY_STARS, safe).apply()
        }
        return safe
    }

    private fun resetDailyCounterIfNeeded(today: String) {
        val savedDate = safeDate(KEY_DAILY_DATE)
        when {
            savedDate == null -> {
                prefs.edit()
                    .putInt(KEY_DAILY_STARS, 0)
                    .putString(KEY_DAILY_DATE, today)
                    .apply()
            }
            savedDate < today -> {
                prefs.edit()
                    .putInt(KEY_DAILY_STARS, 0)
                    .putString(KEY_DAILY_DATE, today)
                    .apply()
            }
            savedDate > today -> {
                return
            }
        }
    }

    private fun todayKey(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
    }
}
