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

class MainViewModel @JvmOverloads constructor(
    application: Application,
    private val settingsRepository: SettingsRepository = SettingsRepository(application)
) : AndroidViewModel(application) {
    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _availableTowers = MutableStateFlow(
        listOf(
            Tower("t1", "Teh Tarik", 150, Color.Blue, stallType = StallType.TEH_TARIK, range = 3f),
            Tower("t2", "Satay", 200, Color.Red, stallType = StallType.SATAY, range = 2.5f, damage = 5, fireRateMs = 500),
            Tower("t3", "Chicken Rice", 100, Color.Yellow, stallType = StallType.CHICKEN_RICE, range = 4f, damage = 15, fireRateMs = 700),
            Tower("t4", "Ice Kachang", 250, Color.Cyan, stallType = StallType.ICE_KACHANG, range = 3.5f, damage = 2, fireRateMs = 1500)
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

        val pillarCount = (columns * rows) / 8
        var pillarsPlaced = 0
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

        while (pillarsPlaced < pillarCount) {
            val bx = random.nextInt(columns)
            val by = random.nextInt(rows)
            if (tempGrid[by][bx].type == CellType.EMPTY) {
                // Try placing pillar
                tempGrid[by][bx] = tempGrid[by][bx].copy(type = CellType.PILLAR)

                // Check if path still exists
                val blocked = mutableSetOf<Position>()
                tempGrid.forEach { row ->
                    row.forEach { cell ->
                        if (cell.type == CellType.PILLAR) blocked.add(cell.position)
                    }
                }

                val path = Pathfinding.findPath(startPos, endPos, columns, rows, blocked)
                if (path != null) {
                    pillarsPlaced++
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

            // 0. Update Puddles
            val updatedPuddles = state.puddles.filter { currentTimeMs - it.spawnTimeMs < it.durationMs }
            newState = newState.copy(puddles = updatedPuddles)

            // 1. Spawning
            if (state.waveActive && state.enemiesToSpawn > 0 && currentTimeMs - state.lastSpawnTimeMs > 1000 && state.grid.isNotEmpty()) {
                val startPos = state.startPosition ?: return@update state
                val endPos = state.endPosition ?: return@update state
                val path = Pathfinding.findPath(
                    startPos, endPos, state.grid[0].size, state.grid.size,
                    getBlockedPositions(state.grid, -1, -1)
                ) ?: emptyList()

                val type = when {
                    state.enemiesToSpawn == 1 && state.currentWave % 5 == 0 -> EnemyType.DELIVERY_RIDER
                    Random().nextFloat() < 0.3f -> EnemyType.TOURIST
                    else -> EnemyType.SALARYMAN
                }

                val enemyHealth = when (type) {
                    EnemyType.SALARYMAN -> 50 + (state.currentWave - 1) * 10
                    EnemyType.TOURIST -> 100 + (state.currentWave - 1) * 20
                    EnemyType.DELIVERY_RIDER -> 500 + (state.currentWave - 1) * 100
                }

                val speed = when (type) {
                    EnemyType.SALARYMAN -> 0.08f
                    EnemyType.TOURIST -> 0.04f
                    EnemyType.DELIVERY_RIDER -> 0.06f
                }

                val newEnemy = Enemy(
                    id = UUID.randomUUID().toString(),
                    type = type,
                    health = enemyHealth,
                    maxHealth = enemyHealth,
                    position = PrecisePosition(startPos.x.toFloat(), startPos.y.toFloat()),
                    baseSpeed = speed,
                    currentSpeed = speed,
                    path = path,
                    currentPathIndex = 0,
                    reward = if (type == EnemyType.DELIVERY_RIDER) 100 else 20
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

                // Handle Freeze
                if (enemy.freezeDurationMs > 0) {
                    return@mapNotNull enemy.copy(freezeDurationMs = Math.max(0, enemy.freezeDurationMs - 32))
                }

                // Handle Tourist Stops
                var isStopped = enemy.isStopped
                var stopDurationMs = enemy.stopDurationMs
                var lastStopMs = enemy.lastStopMs

                if (enemy.type == EnemyType.TOURIST) {
                    if (isStopped) {
                        stopDurationMs -= 32
                        if (stopDurationMs <= 0) {
                            isStopped = false
                            lastStopMs = currentTimeMs
                        }
                    } else if (currentTimeMs - lastStopMs > 8000) {
                        isStopped = true
                        stopDurationMs = 2000L
                    }
                }

                if (isStopped) {
                    return@mapNotNull enemy.copy(isStopped = isStopped, stopDurationMs = stopDurationMs, lastStopMs = lastStopMs)
                }

                // Calculate Speed (Slow effect)
                var speedMultiplier = 1.0f
                if (enemy.type != EnemyType.DELIVERY_RIDER) {
                    val inPuddle = newState.puddles.any { puddle ->
                        val dx = enemy.position.x - puddle.position.x
                        val dy = enemy.position.y - puddle.position.y
                        Math.sqrt((dx * dx + dy * dy).toDouble()) < 0.8
                    }
                    if (inPuddle) speedMultiplier = 0.6f
                }
                val effectiveSpeed = enemy.baseSpeed * speedMultiplier

                val targetIndex = enemy.currentPathIndex + 1
                if (targetIndex >= enemy.path.size) {
                    // Reached end - occupy a table
                    newState = newState.copy(health = Math.max(0, newState.health - 1))
                    return@mapNotNull null
                }

                val target = enemy.path[targetIndex]
                val dx = target.x - enemy.position.x
                val dy = target.y - enemy.position.y
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                if (dist < effectiveSpeed) {
                    enemy.copy(
                        position = PrecisePosition(target.x.toFloat(), target.y.toFloat()),
                        currentPathIndex = targetIndex,
                        currentSpeed = effectiveSpeed,
                        isStopped = isStopped,
                        stopDurationMs = stopDurationMs,
                        lastStopMs = lastStopMs
                    )
                } else {
                    enemy.copy(
                        position = PrecisePosition(
                            enemy.position.x + (dx / dist) * effectiveSpeed,
                            enemy.position.y + (dy / dist) * effectiveSpeed
                        ),
                        currentSpeed = effectiveSpeed,
                        isStopped = isStopped,
                        stopDurationMs = stopDurationMs,
                        lastStopMs = lastStopMs
                    )
                }
            }
            newState = newState.copy(enemies = updatedEnemies)

            // 3. Tower Firing
            val newProjectiles = newState.projectiles.toMutableList()
            val newPuddles = newState.puddles.toMutableList()
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
                            var updatedTower = tower.copy(lastFiredMs = currentTimeMs)

                            when (tower.stallType) {
                                StallType.CHICKEN_RICE -> {
                                    newProjectiles.add(
                                        Projectile(
                                            id = UUID.randomUUID().toString(),
                                            position = PrecisePosition(cell.position.x.toFloat(), cell.position.y.toFloat()),
                                            targetEnemyId = target.id,
                                            targetPosition = target.position,
                                            damage = tower.damage,
                                            color = tower.color
                                        )
                                    )
                                }
                                StallType.TEH_TARIK -> {
                                    // Spawns StickyPuddle on enemy position
                                    newPuddles.add(StickyPuddle(
                                        id = UUID.randomUUID().toString(),
                                        position = target.position,
                                        spawnTimeMs = currentTimeMs
                                    ))
                                }
                                StallType.SATAY -> {
                                    // Periodic cone-based damage (simulated here by hitting all enemies in a cone)
                                    val dx = target.position.x - cell.position.x
                                    val dy = target.position.y - cell.position.y
                                    val angle = Math.atan2(dy.toDouble(), dx.toDouble()).toFloat()
                                    updatedTower = updatedTower.copy(rotation = angle)

                                    // Immediate AoE in cone
                                    newState.enemies.forEach { enemy ->
                                        val edx = enemy.position.x - cell.position.x
                                        val edy = enemy.position.y - cell.position.y
                                        val dist = Math.sqrt((edx * edx + edy * edy).toDouble())
                                        if (dist <= tower.range) {
                                            val eAngle = Math.atan2(edy.toDouble(), edx.toDouble()).toFloat()
                                            var diff = Math.abs(eAngle - angle)
                                            if (diff > Math.PI) diff = (2 * Math.PI - diff).toFloat()
                                            if (diff < Math.PI / 4) { // 45 degree cone
                                                // Apply direct damage in ViewModel update loop
                                                // (Simplification: we'll use hitEnemies map later if we were using projectiles,
                                                // but here we just apply it)
                                            }
                                        }
                                    }
                                    // We'll stick to a special projectile for Satay too for consistency with damage logic
                                    newProjectiles.add(
                                        Projectile(
                                            id = UUID.randomUUID().toString(),
                                            position = PrecisePosition(cell.position.x.toFloat(), cell.position.y.toFloat()),
                                            targetEnemyId = null,
                                            targetPosition = target.position,
                                            damage = tower.damage,
                                            color = tower.color,
                                            speed = 0.5f // fast "puff"
                                        )
                                    )
                                }
                                StallType.ICE_KACHANG -> {
                                    newProjectiles.add(
                                        Projectile(
                                            id = UUID.randomUUID().toString(),
                                            position = PrecisePosition(cell.position.x.toFloat(), cell.position.y.toFloat()),
                                            targetEnemyId = target.id,
                                            targetPosition = target.position,
                                            damage = tower.damage,
                                            color = tower.color,
                                            isFreeze = true
                                        )
                                    )
                                }
                            }
                            cell.copy(tower = updatedTower)
                        } else cell
                    } else cell
                }
            }
            newState = newState.copy(grid = updatedGrid, projectiles = newProjectiles, puddles = newPuddles)

            // 4. Projectile Movement and Collision
            val finalProjectiles = mutableListOf<Projectile>()
            val hitEnemies = mutableMapOf<String, Int>() // enemyId to damage
            val frozenEnemies = mutableSetOf<String>()

            newState.projectiles.forEach { proj ->
                val targetPos = if (proj.targetEnemyId != null) {
                    newState.enemies.find { it.id == proj.targetEnemyId }?.position ?: proj.targetPosition
                } else {
                    proj.targetPosition
                }

                val dx = targetPos.x - proj.position.x
                val dy = targetPos.y - proj.position.y
                val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                if (dist < proj.speed) {
                    if (proj.targetEnemyId != null) {
                        hitEnemies[proj.targetEnemyId] = hitEnemies.getOrDefault(proj.targetEnemyId, 0) + proj.damage
                        if (proj.isFreeze) {
                            frozenEnemies.add(proj.targetEnemyId)
                        }
                    } else {
                        // AoE damage at targetPosition (for Satay)
                        newState.enemies.forEach { enemy ->
                            val edx = enemy.position.x - proj.targetPosition.x
                            val edy = enemy.position.y - proj.targetPosition.y
                            if (Math.sqrt((edx * edx + edy * edy).toDouble()) <= 1.0) {
                                hitEnemies[enemy.id] = hitEnemies.getOrDefault(enemy.id, 0) + proj.damage
                            }
                        }
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

                if (damageTaken > 0 || frozenEnemies.contains(enemy.id)) {
                    val newHealth = Math.max(0, enemy.health - damageTaken)
                    var updatedEnemy = enemy.copy(health = newHealth)

                    if (frozenEnemies.contains(enemy.id)) {
                        updatedEnemy = updatedEnemy.copy(freezeDurationMs = 1500L)
                    }

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

        if (towerToPlace != null && currentState.gold >= towerToPlace.cost && currentState.grid.isNotEmpty()) {
            val cell = currentState.grid[y][x]
            if (cell.type == CellType.EMPTY && cell.tower == null) {
                // Check if blocking path
                val blockedPositions = getBlockedPositions(currentState.grid, x, y)
                val startPos = currentState.startPosition ?: return
                val endPos = currentState.endPosition ?: return
                val path = Pathfinding.findPath(
                    startPos,
                    endPos,
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
                if (cell.tower != null || cell.type == CellType.PILLAR) {
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
