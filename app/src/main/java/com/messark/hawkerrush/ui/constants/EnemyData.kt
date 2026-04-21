package com.messark.hawkerrush.ui.constants

import com.messark.hawkerrush.model.*

// Constants related to enemy behavior and balance
object EnemyConstants {
    // These are now handled per-enemy in EnemyConfig, but fallbacks are useful
    const val DEFAULT_TOURIST_COOLDOWN_MS = 8000L
    const val DEFAULT_TOURIST_STOP_DURATION_MS = 2000L
    // HP scaling per wave is fundamental to enemy progression.
    // It could be here or within EnemyConfig if intended to be per-enemy tunable.
    // For now, let's assume a global HP scaling factor.
    const val ENEMY_HP_SCALE_PER_WAVE = 1.1
}

// Data class to hold the static configuration for an enemy type
data class EnemyConfig(
    val type: EnemyType,
    val name: String,
    val description: String,
    val baseHp: Int,
    val baseSpeed: Float,
    val reward: Int,
    val spriteRow: Int,
    // Enemy-specific timing parameters for behavior
    val touristCooldownMs: Long? = null,
    val touristStopDurationMs: Long? = null
)

object EnemyData {
    val configs = mapOf(
        EnemyType.SALARYMAN to EnemyConfig(
            type = EnemyType.SALARYMAN,
            name = "Salaryman",
            description = "The fast-paced office worker. They move quickly across the grid, eager to reach their destination. Their high speed makes them difficult to hit, but they don't have much health.",
            baseHp = 50,
            baseSpeed = 0.08f,
            reward = 20,
            spriteRow = 2
        ),
        EnemyType.TOURIST to EnemyConfig(
            type = EnemyType.TOURIST,
            name = "Tourist",
            description = "A curious visitor who frequently stops to take pictures of the local sights. While stationary, they are easy targets for your stalls, but they have more health than a Salaryman.",
            baseHp = 100,
            baseSpeed = 0.04f,
            reward = 20,
            spriteRow = 1,
            // Tourist-specific timing parameters
            touristCooldownMs = 8000L,
            touristStopDurationMs = 2000L
        ),
        EnemyType.AUNTIE to EnemyConfig(
            type = EnemyType.AUNTIE,
            name = "Auntie",
            description = "A veteran of the hawker scene. She moves slowly and deliberately, but possesses high health. It takes sustained fire from multiple stalls to stop her progress.",
            baseHp = 150,
            baseSpeed = 0.03f,
            reward = 20,
            spriteRow = 0
        ),
        EnemyType.DELIVERY_RIDER to EnemyConfig(
            type = EnemyType.DELIVERY_RIDER,
            name = "Delivery Rider",
            description = "A formidable boss on two wheels. He has massive health and moves at a significant speed. He is particularly cautious on wet surfaces, slowing down considerably when passing through sticky puddles.",
            baseHp = 500,
            baseSpeed = 0.06f,
            reward = 100,
            spriteRow = 3
        )
    )
}

