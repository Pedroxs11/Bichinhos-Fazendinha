package com.minhafazendinha.game

/**
 * Compatibility target for the farm completion branch.
 * The tap counter already prevents repeated completion; this flag only mirrors
 * the visual-disable assignment until the farm action button is refactored.
 */
internal object it {
    var isEnabled: Boolean = true
}
