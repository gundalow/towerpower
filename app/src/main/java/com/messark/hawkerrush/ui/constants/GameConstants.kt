package com.messark.hawkerrush.ui.constants

// Global constants for game mechanics and parameters
object GameConstants {
    const val GAME_TICK_DURATION_MS = 32L
    const val ENEMY_SPAWN_INTERVAL_MS = 1000L
    const val DELIVERY_RIDER_WAVE_LIMIT = 30
    const val ENEMY_GENERATION_ATTEMPTS_LIMIT = 100
    const val INITIAL_ENEMY_BUDGET_WAVE_6 = 883.0
    const val ENEMY_BUDGET_MULTIPLIER_NORMAL = 1.2
    const val ENEMY_BUDGET_MULTIPLIER_BOSS = 1.44
    const val SPEED_BOOST_MULTIPLIER = 1.5f // Added constant for speed boost
    const val PUDDLE_EFFECT_RADIUS_THRESHOLD = 0.8f // Threshold for enemy proximity to puddle
}
