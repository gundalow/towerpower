package com.example.app.model

import androidx.compose.ui.graphics.Color

data class Position(val x: Int, val y: Int)

enum class CellType {
    EMPTY, PATH, START, END
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
    val range: Float = 2f,
    val damage: Int = 10
)

data class Enemy(
    val id: String,
    val health: Int,
    val maxHealth: Int,
    val position: Position, // In-grid position or more precise? For now, let's keep it simple.
    val speed: Float = 1f
)

data class GameState(
    val grid: List<List<GridCell>> = emptyList(),
    val health: Int = 100,
    val gold: Int = 500,
    val selectedTowerType: Tower? = null,
    val enemies: List<Enemy> = emptyList(),
    val startPosition: Position? = null,
    val endPosition: Position? = null
)
