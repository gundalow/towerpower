package com.messark.tower

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.messark.tower.model.*
import com.messark.tower.utils.Pathfinding
import com.messark.tower.utils.SettingsRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.*

class MainViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository = SettingsRepository(application)
) : AndroidViewModel(application) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _availableTowers = MutableStateFlow(
        listOf(
            Tower("t1", "Basic", 100, Color.Blue),
            Tower("t2", "Fast", 150, Color.Cyan, range = 4f, fireRateMs = 500),
            Tower("t3", "Strong", 200, Color.Magenta, damage = 30),
            Tower("t4", "Sniper", 250, Color.Green, range = 8f, fireRateMs = 2000),
            Tower("t5", "Splash", 300, Color.Yellow, type = TowerType.SPLASH),
            Tower("t6", "Slow", 180, Color.Gray)
        )
    )
    val availableTowers: StateFlow<List<Tower>> = _availableTowers.asStateFlow()

    private var gameJob: Job? = null
    private var lastHapticTimeMs = 0L

    private val _hapticEvents = MutableSharedFlow<Unit>()
    val hapticEvents: SharedFlow<Unit> = _hapticEvents.asSharedFlow()

    init {
        initializeGrid(8, 8) // Start with 8x8 as requested
        startGameLoop()
    }

    private fun initializeGrid(columns: Int, rows: Int) {
        val edges = mutableListOf<Position>()
        for (x in 0 until columns) {
            edges.add(Position(x, 0))
            edges.add(Position(x, rows - 1))
        }
        for (y in 1 until rows - 1) {
            edges.add(Position(0, y))
            edges.add(Position(columns - 1, y))
        }

        var startPos: Position
        var endPos: Position
        val random = Random()

        do {
            startPos = edges[random.nextInt(edges.size)]
            endPos = edges[random.nextInt(edges.size)]
        } while (getEdgeDistance(startPos, endPos, columns, rows) < 5)

        val boulderCount = (columns * rows) / 8
        var bouldersPlaced = 0
        val tempGrid = MutableList(rows) { y ->
            MutableList(columns) { x ->
                val type = when {
                    x == startPos.x && y == startPos.y -> CellType.START
                    x == endPos.x && y == endPos.y -> CellType.END
                    else -> CellType.EMPTY
                }
                GridCell(Position(x, y), type)
            }
        }

        while (bouldersPlaced < boulderCount) {
            val bx = random.nextInt(columns)
            val by = random.nextInt(rows)
            if (tempGrid[by][bx].type == CellType.EMPTY) {
                // Try placing boulder
                tempGrid[by][bx] = tempGrid[by][bx].copy(type = CellType.BOULDER)

                // Check if path still exists
                val blocked = mutableSetOf<Position>()
                tempGrid.forEach { row ->
                    row.forEach { cell ->
                        if (cell.type == CellType.BOULDER) blocked.add(cell.position)
                    }
                }

                val path = Pathfinding.findPath(startPos, endPos, columns, rows, blocked)
                if (path != null) {
                    bouldersPlaced++
                } else {
                    // Revert
                    tempGrid[by][bx] = tempGrid[by][bx].copy(type = CellType.EMPTY)
                }
            }
        }

        _gameState.update { it.copy(
            grid = tempGrid,
            startPosition = startPos,
            endPosition = endPos
        ) }
    }

    private fun getEdgeDistance(p1: Position, p2: Position, width: Int, height: Int): Int {
        fun getEdgeIndex(p: Position): Int {
            return when {
                p.y == 0 -> p.x
                p.x == width - 1 -> width - 1 + p.y
                p.y == height - 1 -> (width - 1) + (height - 1) + (width - 1 - p.x)
                p.x == 0 -> (width - 1) * 2 + (height - 1) + (height - 1 - p.y)
                else -> 0
            }
        }

        val i1 = getEdgeIndex(p1)
        val i2 = getEdgeIndex(p2)
        val totalCells = (width - 1) * 2 + (height - 1) * 2
        val dist = Math.abs(i1 - i2)
        return Math.min(dist, totalCells - dist)
    }

    fun selectTower(tower: Tower) {
        _gameState.update { it.copy(selectedTowerType = tower) }
    }

    fun startWave() {
        val currentState = _gameState.value
        if (currentState.waveActive) return

        val newWave = currentState.currentWave + 1
        _gameState.update {
            it.copy(
                waveActive = true,
                currentWave = newWave,
                enemiesToSpawn = 5,
                lastSpawnTimeMs = System.currentTimeMillis()
            )
        }
    }

    private fun startGameLoop() {
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (isActive) {
                val startTime = System.currentTimeMillis()
                updateGame(startTime)
                val delayTime = 32L - (System.currentTimeMillis() - startTime)
                if (delayTime > 0) delay(delayTime)
            }
        }
    }

    private fun updateGame(currentTimeMs: Long) {
        _gameState.update { state ->
            var newState = state

            // 1. Spawning
            if (state.waveActive && state.enemiesToSpawn > 0 && currentTimeMs - state.lastSpawnTimeMs > 1000) {
                val startPos = state.startPosition!!
                val endPos = state.endPosition!!
                val path = Pathfinding.findPath(
                    startPos, endPos, state.grid[0].size, state.grid.size,
                    getBlockedPositions(state.grid, -1, -1)
                ) ?: emptyList()

                val enemyHealth = 50 + (state.currentWave - 1) * 10
                val newEnemy = Enemy(
                    id = UUID.randomUUID().toString(),
                    health = enemyHealth,
                    maxHealth = enemyHealth,
                    position = PrecisePosition(startPos.x.toFloat(), startPos.y.toFloat()),
                    path = path,
                    currentPathIndex = 0
                )
                newState = newState.copy(
                    enemies = newState.enemies + newEnemy,
                    enemiesToSpawn = newState.enemiesToSpawn - 1,
                    lastSpawnTimeMs = currentTimeMs
                )
            }

            // 2. Enemy Movement
            val updatedEnemies = newState.enemies.mapNotNull { enemy ->
                if (enemy.isDead) return@mapNotNull null

                val targetIndex = enemy.currentPathIndex + 1
                if (targetIndex >= enemy.path.size) {
                    // Reached end
                    newState = newState.copy(health = Math.max(0, newState.health - 10))
                    return@mapNotNull null
                }

                val target = enemy.path[targetIndex]
                val dx = target.x - enemy.position.x
                val dy = target.y - enemy.position.y
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                if (dist < enemy.speed) {
                    enemy.copy(
                        position = PrecisePosition(target.x.toFloat(), target.y.toFloat()),
                        currentPathIndex = targetIndex
                    )
                } else {
                    enemy.copy(
                        position = PrecisePosition(
                            enemy.position.x + (dx / dist) * enemy.speed,
                            enemy.position.y + (dy / dist) * enemy.speed
                        )
                    )
                }
            }
            newState = newState.copy(enemies = updatedEnemies)

            // 3. Tower Firing
            val newProjectiles = newState.projectiles.toMutableList()
            val updatedGrid = newState.grid.map { row ->
                row.map { cell ->
                    val tower = cell.tower
                    if (tower != null && currentTimeMs - tower.lastFiredMs >= tower.fireRateMs) {
                        // Find target
                        val target = newState.enemies.firstOrNull { enemy ->
                            val dx = enemy.position.x - cell.position.x
                            val dy = enemy.position.y - cell.position.y
                            Math.sqrt((dx * dx + dy * dy).toDouble()) <= tower.range
                        }

                        if (target != null) {
                            newProjectiles.add(
                                Projectile(
                                    id = UUID.randomUUID().toString(),
                                    position = PrecisePosition(cell.position.x.toFloat(), cell.position.y.toFloat()),
                                    targetEnemyId = if (tower.type == TowerType.SINGLE_TARGET) target.id else null,
                                    targetPosition = target.position,
                                    damage = tower.damage,
                                    color = tower.color,
                                    isSplash = tower.type == TowerType.SPLASH
                                )
                            )
                            cell.copy(tower = tower.copy(lastFiredMs = currentTimeMs))
                        } else cell
                    } else cell
                }
            }
            newState = newState.copy(grid = updatedGrid, projectiles = newProjectiles)

            // 4. Projectile Movement and Collision
            val finalProjectiles = mutableListOf<Projectile>()
            val hitEnemies = mutableMapOf<String, Int>() // enemyId to damage
            var splashDamageLocations = mutableListOf<Pair<PrecisePosition, Int>>()

            newState.projectiles.forEach { proj ->
                val targetPos = if (proj.isSplash) {
                    proj.targetPosition
                } else {
                    newState.enemies.find { it.id == proj.targetEnemyId }?.position ?: proj.targetPosition
                }

                val dx = targetPos.x - proj.position.x
                val dy = targetPos.y - proj.position.y
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                if (dist < proj.speed) {
                    if (proj.isSplash) {
                        splashDamageLocations.add(targetPos to proj.damage)
                    } else if (proj.targetEnemyId != null) {
                        hitEnemies[proj.targetEnemyId] = hitEnemies.getOrDefault(proj.targetEnemyId, 0) + proj.damage
                    }
                } else {
                    finalProjectiles.add(proj.copy(
                        position = PrecisePosition(
                            proj.position.x + (dx / dist) * proj.speed,
                            proj.position.y + (dy / dist) * proj.speed
                        )
                    ))
                }
            }

            // Apply damage
            val finalEnemies = newState.enemies.map { enemy ->
                var damageTaken = hitEnemies.getOrDefault(enemy.id, 0)
                splashDamageLocations.forEach { (splashPos, damage) ->
                    val dx = enemy.position.x - splashPos.x
                    val dy = enemy.position.y - splashPos.y
                    if (Math.sqrt((dx * dx + dy * dy).toDouble()) <= 1.5) { // Splash radius
                        damageTaken += damage
                    }
                }

                if (damageTaken > 0) {
                    val newHealth = enemy.health - damageTaken
                    if (newHealth <= 0) {
                        newState = newState.copy(gold = newState.gold + enemy.reward)
                        // Trigger haptic if not throttled
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastHapticTimeMs >= 1000) {
                            viewModelScope.launch {
                                val settings = settingsRepository.settingsFlow.first()
                                if (settings.hapticEnabled) {
                                    _hapticEvents.emit(Unit)
                                }
                            }
                            lastHapticTimeMs = currentTime
                        }
                        enemy.copy(health = 0, isDead = true)
                    } else {
                        enemy.copy(health = newHealth)
                    }
                } else enemy
            }.filter { !it.isDead }

            newState = newState.copy(
                enemies = finalEnemies,
                projectiles = finalProjectiles
            )

            // 5. Check wave end
            if (newState.waveActive && newState.enemiesToSpawn == 0 && newState.enemies.isEmpty()) {
                newState = newState.copy(waveActive = false)
            }

            newState
        }
    }

    fun onCellClick(x: Int, y: Int) {
        val currentState = _gameState.value
        val towerToPlace = currentState.selectedTowerType

        if (towerToPlace != null && currentState.gold >= towerToPlace.cost) {
            val cell = currentState.grid[y][x]
            if (cell.type == CellType.EMPTY && cell.tower == null) {
                // Check if blocking path
                val blockedPositions = getBlockedPositions(currentState.grid, x, y)
                val path = Pathfinding.findPath(
                    currentState.startPosition!!,
                    currentState.endPosition!!,
                    currentState.grid[0].size,
                    currentState.grid.size,
                    blockedPositions
                )

                if (path != null) {
                    val newGrid = currentState.grid.mapIndexed { rowIdx, row ->
                        if (rowIdx == y) {
                            row.mapIndexed { colIdx, gridCell ->
                                if (colIdx == x) {
                                    gridCell.copy(tower = towerToPlace.copy(id = UUID.randomUUID().toString()))
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

    private fun getBlockedPositions(grid: List<List<GridCell>>, newX: Int, newY: Int): Set<Position> {
        val blocked = mutableSetOf<Position>()
        grid.forEach { row ->
            row.forEach { cell ->
                if (cell.tower != null || cell.type == CellType.BOULDER) {
                    blocked.add(cell.position)
                }
            }
        }
        if (newX != -1 && newY != -1) {
            blocked.add(Position(newX, newY))
        }
        return blocked
    }
}
