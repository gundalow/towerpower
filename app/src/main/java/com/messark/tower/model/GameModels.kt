package com.messark.tower.model

import androidx.compose.ui.graphics.Color

data class Position(val x: Int, val y: Int)

data class PrecisePosition(val x: Float, val y: Float)

enum class CellType {
    EMPTY, PATH, START, END, BOULDER
}

data class GridCell(
    val position: Position,
    val type: CellType = CellType.EMPTY,
    val tower: Tower? = null
)

data class Tower(
    val id: String,
    val name: String,
    val cost: Int,
    val color: Color,
    val range: Float = 3f, // Grid units
    val damage: Int = 10,
    val fireRateMs: Long = 1000L,
    val lastFiredMs: Long = 0L,
    val type: TowerType = TowerType.SINGLE_TARGET
)

enum class TowerType {
    SINGLE_TARGET, SPLASH
}

data class Enemy(
    val id: String,
    val health: Int,
    val maxHealth: Int,
    val position: PrecisePosition,
    val speed: Float = 0.05f, // Grid units per tick
    val path: List<Position> = emptyList(),
    val currentPathIndex: Int = 0,
    val isDead: Boolean = false,
    val reward: Int = 20
)

data class Projectile(
    val id: String,
    val position: PrecisePosition,
    val targetEnemyId: String?,
    val targetPosition: PrecisePosition, // For splash/ground target
    val damage: Int,
    val speed: Float = 0.2f,
    val color: Color,
    val isSplash: Boolean = false
)

data class GameState(
    val grid: List<List<GridCell>> = emptyList(),
    val health: Int = 100,
    val gold: Int = 500,
    val selectedTowerType: Tower? = null,
    val enemies: List<Enemy> = emptyList(),
    val projectiles: List<Projectile> = emptyList(),
    val startPosition: Position? = null,
    val endPosition: Position? = null,
    val waveActive: Boolean = false,
    val currentWave: Int = 0,
    val enemiesToSpawn: Int = 0,
    val lastSpawnTimeMs: Long = 0L
)
