package com.messark.tower

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import com.messark.tower.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _availableTowers = MutableStateFlow(
        listOf(
            Tower("t1", "Basic", 100, Color.Blue),
            Tower("t2", "Fast", 150, Color.Cyan),
            Tower("t3", "Strong", 200, Color.Magenta),
            Tower("t4", "Sniper", 250, Color.Green),
            Tower("t5", "Splash", 300, Color.Yellow),
            Tower("t6", "Slow", 180, Color.Gray)
        )
    )
    val availableTowers: StateFlow<List<Tower>> = _availableTowers.asStateFlow()

    init {
        initializeGrid(10, 20) // Default grid size
    }

    private fun initializeGrid(columns: Int, rows: Int) {
        val startPos = Position(0, rows / 2)
        val endPos = Position(columns - 1, rows / 2)

        val newGrid = List(rows) { y ->
            List(columns) { x ->
                val type = when {
                    x == startPos.x && y == startPos.y -> CellType.START
                    x == endPos.x && y == endPos.y -> CellType.END
                    else -> CellType.EMPTY
                }
                GridCell(Position(x, y), type)
            }
        }

        _gameState.update { it.copy(
            grid = newGrid,
            startPosition = startPos,
            endPosition = endPos
        ) }
    }

    fun selectTower(tower: Tower) {
        _gameState.update { it.copy(selectedTowerType = tower) }
    }

    fun onCellClick(x: Int, y: Int) {
        val currentState = _gameState.value
        val towerToPlace = currentState.selectedTowerType

        if (towerToPlace != null && currentState.gold >= towerToPlace.cost) {
            val cell = currentState.grid[y][x]
            if (cell.type == CellType.EMPTY && cell.tower == null) {
                val newGrid = currentState.grid.mapIndexed { rowIdx, row ->
                    if (rowIdx == y) {
                        row.mapIndexed { colIdx, gridCell ->
                            if (colIdx == x) {
                                gridCell.copy(tower = towerToPlace)
                            } else gridCell
                        }
                    } else row
                }
                _gameState.update { it.copy(
                    grid = newGrid,
                    gold = it.gold - towerToPlace.cost
                ) }
            }
        }
    }
}
