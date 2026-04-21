package com.messark.hawkerrush.logic

import com.messark.hawkerrush.model.*
import com.messark.hawkerrush.ui.constants.EnemyConfig
import com.messark.hawkerrush.ui.constants.EnemyConstants
import com.messark.hawkerrush.ui.constants.GameConstants
import java.util.*

/**
 * Handles actions and logic related to enemies.
 */
object EnemyBehaviorHandler {

    /**
     * Updates enemy behavior, including special actions like stopping for tourists.
     */
    fun updateSpecialBehavior(enemyConfig: EnemyConfig, enemy: Enemy, currentTimeMs: Long): Enemy {
        // Use enemy-specific constants from EnemyConfig, defaulting if not provided.
        val cooldown = enemyConfig.touristCooldownMs ?: EnemyConstants.DEFAULT_TOURIST_COOLDOWN_MS // Use constant from EnemyConstants
        val stopDuration = enemyConfig.touristStopDurationMs ?: EnemyConstants.DEFAULT_TOURIST_STOP_DURATION_MS // Use constant from EnemyConstants

        var isStopped = enemy.isStopped
        var stopDurationMs = enemy.stopDurationMs
        var lastStopMs = enemy.lastStopMs

        if (isStopped) {
            stopDurationMs -= GameConstants.GAME_TICK_DURATION_MS // Use constant
            if (stopDurationMs <= 0) {
                isStopped = false
                lastStopMs = currentTimeMs
            }
        } else if (currentTimeMs - lastStopMs > cooldown) { // Use config cooldown
            isStopped = true
            stopDurationMs = stopDuration // Use config stop duration
        }
        return enemy.copy(isStopped = isStopped, stopDurationMs = stopDurationMs, lastStopMs = lastStopMs)
    }

    /**
     * Calculates the benefit of an upgrade.
     */
    fun getUpgradeBenefit(currentValue: Float, configValue: Float): String {
        return "+${String.format(Locale.US, "%.1f", currentValue - configValue)}"
    }

    /**
     * Calculates the slow multiplier for enemies when they are in a puddle.
     */
    fun getPuddleSlowMultiplier(enemyType: EnemyType): Float {
        return when (enemyType) {
            EnemyType.DELIVERY_RIDER -> 0.2f
            EnemyType.AUNTIE -> 0.8f
            else -> 0.6f
        }
    }

    /**
     * Calculates the current HP of an enemy based on the wave number.
     */
    fun getEnemyHpForWave(enemyConfig: EnemyConfig, wave: Int): Int {
        return (enemyConfig.baseHp * Math.pow(EnemyConstants.ENEMY_HP_SCALE_PER_WAVE, (wave - 1).toDouble())).toInt() // Use constant
    }

    /**
     * Creates an Enemy instance from its configuration.
     */
    fun createEnemyInstance(
        enemyConfig: EnemyConfig,
        id: String = UUID.randomUUID().toString(),
        wave: Int,
        position: PreciseAxialCoordinate,
        path: List<AxialCoordinate>,
        isFacingLeft: Boolean
    ): Enemy {
        val hp = getEnemyHpForWave(enemyConfig, wave)
        return Enemy(
            id = id,
            type = enemyConfig.type,
            health = hp,
            maxHealth = hp,
            position = position,
            baseSpeed = enemyConfig.baseSpeed,
            currentSpeed = enemyConfig.baseSpeed,
            path = path,
            currentPathIndex = 0,
            reward = enemyConfig.reward,
            isFacingLeft = isFacingLeft
        )
    }
}
