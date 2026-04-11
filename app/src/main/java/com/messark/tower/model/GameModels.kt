package com.messark.tower.model

import androidx.compose.ui.graphics.Color

data class Position(val x: Int, val y: Int)

data class PrecisePosition(val x: Float, val y: Float)

enum class CellType {
    EMPTY, PATH, START, END, PILLAR
}

data class GridCell(
    val position: Position,
    val type: CellType = CellType.EMPTY,
    val tower: Tower? = null
)

enum class StallType {
    TEH_TARIK, SATAY, CHICKEN_RICE, ICE_KACHANG
}

data class Tower(
    val id: String,
    val name: String,
    val cost: Int,
    val color: Color,
    val range: Float = 3f, // Grid units
    val damage: Int = 10,
    val fireRateMs: Long = 1000L,
    val lastFiredMs: Long = 0L,
    val stallType: StallType = StallType.CHICKEN_RICE,
    val rotation: Float = 0f // For Satay cone direction
)

enum class EnemyType {
    SALARYMAN, TOURIST, DELIVERY_RIDER
}

data class Enemy(
    val id: String,
    val type: EnemyType = EnemyType.SALARYMAN,
    val health: Int,
    val maxHealth: Int,
    val position: PrecisePosition,
    val baseSpeed: Float = 0.05f, // Grid units per tick
    val currentSpeed: Float = 0.05f,
    val path: List<Position> = emptyList(),
    val currentPathIndex: Int = 0,
    val isDead: Boolean = false,
    val reward: Int = 20,
    val freezeDurationMs: Long = 0L,
    val lastStopMs: Long = 0L,
    val isStopped: Boolean = false,
    val stopDurationMs: Long = 0L
)

data class Projectile(
    val id: String,
    val position: PrecisePosition,
    val targetEnemyId: String?,
    val targetPosition: PrecisePosition,
    val damage: Int,
    val speed: Float = 0.2f,
    val color: Color,
    val isFreeze: Boolean = false
)

data class StickyPuddle(
    val id: String,
    val position: PrecisePosition,
    val spawnTimeMs: Long,
    val durationMs: Long = 3000L
)

data class GameState(
    val grid: List<List<GridCell>> = emptyList(),
    val health: Int = 10, // Changed to 10 tables
    val gold: Int = 500,
    val selectedTowerType: Tower? = null,
    val enemies: List<Enemy> = emptyList(),
    val projectiles: List<Projectile> = emptyList(),
    val puddles: List<StickyPuddle> = emptyList(),
    val startPosition: Position? = null,
    val endPosition: Position? = null,
    val waveActive: Boolean = false,
    val currentWave: Int = 0,
    val enemiesToSpawn: Int = 0,
    val lastSpawnTimeMs: Long = 0L
)
