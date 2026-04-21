package com.messark.hawkerrush.logic

import androidx.compose.ui.graphics.Color
import com.messark.hawkerrush.model.*
import com.messark.hawkerrush.registry.FireResult
import com.messark.hawkerrush.ui.constants.StallData
import com.messark.hawkerrush.ui.constants.EnemyData
import com.messark.hawkerrush.ui.constants.EnemyConstants
import com.messark.hawkerrush.ui.constants.GameConstants // Import GameConstants
import com.messark.hawkerrush.ui.constants.StallConstants
import com.messark.hawkerrush.ui.constants.StallUpgradeCategory
import java.util.*

/**
 * Handles actions and logic related to stalls.
 */
object StallActionHandler {

    /**
     * Calculates the benefit of upgrading a stall based on its category and level.
     */
    fun getUpgradeBenefit(stallConfig: StallConfig, category: String, level: Int): String {
        if (level <= 0) return ""

        return when (category) {
            "Damage" -> {
                val baseDamage = stallConfig.damage
                val increasePerLevel = getUpgradeDamageIncrease(baseDamage)
                var currentDamage = baseDamage
                for (l in 1..level) {
                    currentDamage += increasePerLevel
                    if (l % 10 == 0) {
                        currentDamage = Math.round(currentDamage * 1.25f)
                    }
                }
                val percentage = Math.round(((currentDamage - baseDamage).toFloat() / baseDamage) * 100)
                "+$percentage%"
            }
            "Rate" -> {
                val baseRate = stallConfig.fireRateMs
                val rateReduction = (baseRate * 0.1f).toLong()
                var currentRate = baseRate
                for (l in 1..level) {
                    currentRate = Math.max(50L, currentRate - rateReduction)
                    if (l % 10 == 0) {
                        currentRate = Math.max(50L, Math.round(currentRate * 0.75))
                    }
                }
                val percentage = Math.round(((baseRate - currentRate).toFloat() / baseRate) * 100)
                "+$percentage%"
            }
            "Range" -> {
                var currentRange = stallConfig.range
                for (l in 1..level) {
                    currentRange += 0.5f
                    if (l % 10 == 0) {
                        currentRange *= 1.25f
                    }
                }
                "+${String.format("%.1f", currentRange - stallConfig.range)}"
            }
            "Radius" -> {
                var currentRadius = stallConfig.aoeRadius
                for (l in 1..level) {
                    currentRadius += 0.2f
                    if (l % 10 == 0) {
                        currentRadius *= 1.25f
                    }
                }
                "+${String.format("%.1f", currentRadius - stallConfig.aoeRadius)}"
            }
            "Duration" -> {
                var currentDuration = stallConfig.effectDurationMs
                for (l in 1..level) {
                    currentDuration += 500
                    if (l % 10 == 0) {
                        currentDuration = Math.round(currentDuration * 1.25f).toLong()
                    }
                }
                "+${currentDuration - stallConfig.effectDurationMs}ms"
            }
            "Effect" -> {
                var currentEffect = stallConfig.freezeDurationMs
                for (l in 1..level) {
                    currentEffect += 100
                    if (l % 10 == 0) {
                        currentEffect = Math.round(currentEffect * 1.25f).toLong()
                    }
                }
                "+${currentEffect - stallConfig.freezeDurationMs}ms"
            }
            else -> ""
        }
    }

    /**
     * Calculates the damage increase from upgrades for Chicken Rice.
     */
    private fun getUpgradeDamageIncrease(baseDamage: Int): Int {
        return (baseDamage * 0.3f).toInt() + 2
    }

    /**
     * Applies damage modifiers based on stall and enemy types.
     */
    fun applyDamageModifiers(stallConfig: StallConfig, enemy: Enemy, baseDamage: Float): Float {
        return when (stallConfig.type) {
            StallType.SATAY -> when (enemy.type) {
                EnemyType.TOURIST -> baseDamage * 2f
                EnemyType.AUNTIE -> baseDamage * 0.5f
                else -> baseDamage
            }
            StallType.DURIAN -> when (enemy.type) {
                EnemyType.DELIVERY_RIDER -> baseDamage * 1.5f
                else -> baseDamage
            }
            else -> baseDamage
        }
    }

    /**
     * Determines the freeze duration modifier based on enemy type.
     */
    fun getFreezeModifier(stallConfig: StallConfig, enemy: Enemy, baseDuration: Long): Long {
        if (stallConfig.type != StallType.ICE_KACHANG) return 0L
        return when (enemy.type) {
            EnemyType.SALARYMAN -> baseDuration * 2
            EnemyType.TOURIST -> baseDuration / 2
            else -> baseDuration
        }
    }

    /**
     * Applies a speed boost effect for Durian on Salaryman enemies.
     */
    fun getSpeedBoost(stallConfig: StallConfig, enemy: Enemy): Long {
        if (stallConfig.type == StallType.DURIAN && enemy.type == EnemyType.SALARYMAN) {
            return 2000L
        }
        return 0L
    }

    /**
     * Executes the firing logic for a stall, returning a FireResult (NewProjectile or NewPuddle).
     */
    fun fire(
        stallConfig: StallConfig,
        stall: Stall, // The actual instance of the stall on the board
        stallCoord: AxialCoordinate,
        target: Enemy,
        currentTimeMs: Long
    ): FireResult {
        val stallPos = PreciseAxialCoordinate(stallCoord.q.toFloat(), stallCoord.r.toFloat())
        return when (stallConfig.type) {
            StallType.TEH_TARIK -> FireResult.NewPuddle(
                StickyPuddle(
                    id = UUID.randomUUID().toString(),
                    position = target.position,
                    spawnTimeMs = currentTimeMs,
                    durationMs = stall.effectDurationMs,
                    sourceStallCoord = stallCoord,
                    sourceStallId = stall.id
                )
            )
            StallType.SATAY -> {
                val dq = target.position.q - stallCoord.q
                val dr = target.position.r - stallCoord.r
                val angle = Math.atan2(dr.toDouble(), dq.toDouble()).toFloat()
                FireResult.NewProjectile(
                    projectile = Projectile(
                        id = UUID.randomUUID().toString(),
                        position = stallPos,
                        targetEnemyId = null,
                        targetPosition = target.position,
                        damage = stall.damage,
                        color = stallConfig.projectileColor,
                        speed = stallConfig.projectileSpeed,
                        aoeRadius = stall.aoeRadius,
                        isArc = stallConfig.isArc,
                        startPosition = stallPos,
                        sourceStallType = stallConfig.type,
                        sourceStallCoord = stallCoord,
                        sourceStallId = stall.id
                    ),
                    updatedStall = stall.copy(rotation = angle)
                )
            }
            else -> FireResult.NewProjectile(
                projectile = Projectile(
                    id = UUID.randomUUID().toString(),
                    position = stallPos,
                    targetEnemyId = target.id,
                    targetPosition = target.position,
                    damage = stall.damage,
                    color = stall.color,
                    isFreeze = stallConfig.type == StallType.ICE_KACHANG,
                    freezeDurationMs = stall.freezeDurationMs,
                    aoeRadius = stall.aoeRadius,
                    sourceStallType = stallConfig.type,
                    sourceStallCoord = stallCoord,
                    sourceStallId = stall.id
                )
            )
        }
    }

    /**
     * Creates a Stall instance from its configuration.
     */
    fun createStallInstance(
        stallConfig: StallConfig,
        id: String = UUID.randomUUID().toString(),
        initialUpgradeCount: Int = 0,
        initialUpgrades: Map<String, Int> = emptyMap(),
        initialLegendaryPrefix: String? = null,
        initialLegendarySuffix: String? = null,
        initialNamingCategories: List<String> = emptyList()
    ): Stall {
        return Stall(
            id = id,
            name = stallConfig.name,
            baseName = stallConfig.name,
            cost = stallConfig.cost,
            color = stallConfig.color,
            range = stallConfig.range,
            damage = stallConfig.damage,
            fireRateMs = stallConfig.fireRateMs,
            stallType = stallConfig.type,
            description = stallConfig.description,
            aoeRadius = stallConfig.aoeRadius,
            effectDurationMs = stallConfig.effectDurationMs,
            freezeDurationMs = stallConfig.freezeDurationMs,
            upgradeCount = initialUpgradeCount,
            upgrades = initialUpgrades,
            totalInvestment = stallConfig.cost,
            targetMode = TargetMode.FIRST,
            legendaryPrefix = initialLegendaryPrefix,
            legendarySuffix = initialLegendarySuffix,
            namingCategories = initialNamingCategories
        )
    }
}

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
     * Calculates the slow multiplier for enemies when they are in a puddle.
     */
    fun getPuddleSlowMultiplier(enemyConfig: EnemyConfig, enemyType: EnemyType): Float {
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
