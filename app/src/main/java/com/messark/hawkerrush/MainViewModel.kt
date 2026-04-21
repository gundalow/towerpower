package com.messark.hawkerrush

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.messark.hawkerrush.logic.EnemyBehaviorHandler
import com.messark.hawkerrush.logic.StallActionHandler
import com.messark.hawkerrush.model.*
import com.messark.hawkerrush.model.FireResult
import com.messark.hawkerrush.ui.constants.EnemyData
import com.messark.hawkerrush.ui.constants.GameConstants
import com.messark.hawkerrush.ui.constants.StallData
import com.messark.hawkerrush.ui.constants.StallConstants // Import StallConstants
import com.messark.hawkerrush.ui.constants.StallUpgradeCategory // Import StallUpgradeCategory
import com.messark.hawkerrush.utils.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*

class MainViewModel @JvmOverloads constructor(
    application: Application,
    private val settingsRepository: SettingsRepository = SettingsRepository(application),
    private val gameStateRepository: GameStateRepository = GameStateRepository(application)
) : AndroidViewModel(application) {
    internal val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _logoVisible = MutableStateFlow(true)
    val logoVisible: StateFlow<Boolean> = _logoVisible.asStateFlow()

    private val enemyTiers = listOf(
        EnemyType.SALARYMAN,
        EnemyType.TOURIST,
        EnemyType.AUNTIE,
        EnemyType.DELIVERY_RIDER
    )

    private val _availableStalls = MutableStateFlow(
        // Use StallActionHandler to create instances from StallData configs
        StallData.configs.values.map { StallActionHandler.createStallInstance(it) }
    )
    val availableStalls: StateFlow<List<Stall>> = _availableStalls.asStateFlow()

    private var gameJob: Job? = null
    private var lastHapticTimeMs = 0L

    private val _hapticEvents = MutableSharedFlow<Unit>()
    val hapticEvents: SharedFlow<Unit> = _hapticEvents.asSharedFlow()

    init {
        initializeGame()
        startGameLoop()
    }

    private fun initializeGame() {
        val (hexes, startPos, endPos) = MapGenerator.generateRandomVerticalMap(width = 8, height = 16)

        _gameState.update { it.copy(
            hexes = hexes,
            startPosition = startPos,
            endPosition = endPos,
            gold = 500, // Start with some gold to place stalls
            currentScreen = AppScreen.LOADING
        ) }
    }

    fun navigateTo(screen: AppScreen) {
        _gameState.update { it.copy(currentScreen = screen) }
    }

    fun hideLogo() {
        _logoVisible.value = false
    }

    fun triggerHaptic() {
        viewModelScope.launch {
            if (settingsRepository.settingsFlow.first().hapticEnabled) {
                _hapticEvents.emit(Unit)
            }
        }
    }

    fun updateHapticSetting(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(hapticEnabled = enabled) }
        }
    }

    fun updateTutorialsSetting(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateSettings { it.copy(showTutorials = enabled) }
        }
    }

    fun hasSavedGame(): Boolean = gameStateRepository.hasSavedGame()

    fun resumeGame() {
        val savedState = gameStateRepository.loadGameState()
        if (savedState != null) {
            _gameState.value = savedState
        }
    }

    fun resetGame() {
        gameStateRepository.deleteGameState()
        val (hexes, startPos, endPos) = MapGenerator.generateRandomVerticalMap(width = 8, height = 16)
        _gameState.update {
            GameState(
                currentScreen = AppScreen.GAME,
                hexes = hexes,
                startPosition = startPos,
                endPosition = endPos,
                gold = 500,
                score = 0
            )
        }
    }

    fun selectStall(stall: Stall) {
        _gameState.update { it.copy(selectedStallType = stall) }
    }

    fun startWave() {
        val currentState = _gameState.value
        if (currentState.waveActive || currentState.activeTutorial != null) return

        val newWave = currentState.currentWave + 1
        val enemyList = generateEnemyList(newWave)

        viewModelScope.launch {
            val settings = settingsRepository.settingsFlow.first()
            if (settings.showTutorials) {
                val newEnemyTypes = enemyList.distinct().filter { !settings.shownTutorials.contains("enemy_${it.name.lowercase()}") }
                if (newEnemyTypes.isNotEmpty()) {
                    val firstNewEnemy = newEnemyTypes.first()
                    // Use EnemyData to get config
                    val enemyConfig = EnemyData.configs[firstNewEnemy] ?: return@launch // Should not happen if enemyList is valid
                    val tutorial = TutorialData(
                        id = "enemy_${firstNewEnemy.name.lowercase()}",
                        type = TutorialType.ENEMY,
                        title = enemyConfig.name,
                        description = enemyConfig.description,
                        enemyType = firstNewEnemy
                    )

                    _gameState.update { it.copy(activeTutorial = tutorial) }
                    // Mark as shown
                    settingsRepository.updateSettings {
                        it.copy(shownTutorials = it.shownTutorials + "enemy_${firstNewEnemy.name.lowercase()}")
                    }
                    return@launch
                }
            }

            proceedWithWave(newWave, enemyList)
        }
    }

    private fun proceedWithWave(newWave: Int, enemyList: List<EnemyType>) {
        val isBossWave = newWave % 10 == 0
        val currentTime = System.currentTimeMillis()

        _gameState.update {
            it.copy(
                waveActive = true,
                currentWave = newWave,
                enemiesToSpawn = enemyList.size,
                enemiesToSpawnList = enemyList,
                isBossWave = isBossWave,
                bossWaveTriggerTimeMs = if (isBossWave) currentTime else 0L,
                lastSpawnTimeMs = currentTime
            )
        }
    }

    fun dismissTutorial() {
        val currentState = _gameState.value
        val tutorial = currentState.activeTutorial ?: return

        _gameState.update { it.copy(activeTutorial = null) }

        // If it was an enemy tutorial, we might want to start the wave now
        if (tutorial.type == TutorialType.ENEMY) {
            // Re-check if there are MORE tutorials for this wave (e.g. wave has 2 new enemies)
            startWave()
        }
    }

    fun showStallTutorial(stallType: StallType) {
        // Use StallData to get config
        val stallConfig = StallData.configs[stallType] ?: return // Should not happen
        val tutorial = TutorialData(
            id = "stall_${stallType.name.lowercase()}",
            type = TutorialType.STALL,
            title = stallConfig.tutorialTitle,
            signatureMove = stallConfig.signatureMove,
            description = stallConfig.tutorialDescription,
            stallType = stallType
        )
        _gameState.update { it.copy(activeTutorial = tutorial) }
    }

    private fun generateEnemyList(wave: Int): List<EnemyType> {
        if (wave <= 6) {
            val list = when (wave) {
                1 -> List(5) { EnemyType.SALARYMAN }
                2 -> List(6) { EnemyType.SALARYMAN }
                3 -> List(5) { EnemyType.SALARYMAN } + List(1) { EnemyType.TOURIST }
                4 -> List(6) { EnemyType.SALARYMAN } + List(1) { EnemyType.TOURIST }
                5 -> List(5) { EnemyType.SALARYMAN } + List(2) { EnemyType.TOURIST }
                6 -> List(4) { EnemyType.SALARYMAN } + List(2) { EnemyType.TOURIST } + List(1) { EnemyType.AUNTIE }
                else -> emptyList()
            }
            return list.shuffled()
        }

        // Algorithmic for Wave 7+
        // Calculate budget iteratively for consistency
        var budget = GameConstants.INITIAL_ENEMY_BUDGET_WAVE_6 // Use constant
        for (i in 7..wave) {
            if (i % 10 == 0) {
                budget *= GameConstants.ENEMY_BUDGET_MULTIPLIER_BOSS // Use constant
            } else { // This logic implies normal multiplier is used unless it's a boss wave
                budget *= GameConstants.ENEMY_BUDGET_MULTIPLIER_NORMAL // Use constant
            }
        }

        val enemyList = mutableListOf<EnemyType>()
        var remainingBudget = budget

        val maxTierIndex = minOf((wave - 1) / 2, enemyTiers.size - 1)
        var allowedTiers = enemyTiers.subList(0, maxTierIndex + 1)

        // Only allow Delivery Riders in boss waves until level 30
        if (wave <= GameConstants.DELIVERY_RIDER_WAVE_LIMIT && wave % 10 != 0) {
            allowedTiers = allowedTiers.filter { it != EnemyType.DELIVERY_RIDER }
        }

        var attempts = 0
        while (remainingBudget > 0 && attempts < GameConstants.ENEMY_GENERATION_ATTEMPTS_LIMIT) { // Use constant
            val type = allowedTiers[kotlin.random.Random.nextInt(allowedTiers.size)]
            // Use EnemyData to get config for HP calculation
            val enemyConfig = EnemyData.configs[type] ?: continue // Should not happen
            val hp = EnemyBehaviorHandler.getEnemyHpForWave(enemyConfig, wave)
            if (hp <= remainingBudget) {
                enemyList.add(type)
                remainingBudget -= hp
            } else if (allowedTiers.all { EnemyData.configs[it]?.let { config -> EnemyBehaviorHandler.getEnemyHpForWave(config, wave) } ?: 0 > remainingBudget }) {
                remainingBudget = 0.0 // Exit while loop
            }
            attempts++
        }

        if (enemyList.isEmpty()) {
            enemyList.add(EnemyType.SALARYMAN)
        }

        return enemyList.shuffled()
    }

    // Removed: getEnemyHP function as it's now in EnemyBehaviorHandler

    private fun startGameLoop() {
        gameJob?.cancel()
        gameJob = viewModelScope.launch {
            while (isActive) {
                val startTime = System.currentTimeMillis()
                updateGame(startTime)
                // Use constant for game tick duration
                val delayTime = GameConstants.GAME_TICK_DURATION_MS - (System.currentTimeMillis() - startTime)
                if (delayTime > 0) delay(delayTime)
            }
        }
    }

    /**
     * Updates game state by advancing spawning, movement, and combat.
     *
     * @param currentTimeMs Current game time in milliseconds.
     */
    internal fun updateGame(currentTimeMs: Long) {
        _gameState.update { state ->
            if (state.activeTutorial != null) return@update state
            var newState = state

            // 0. Update Puddles and Visual Effects
            newState = updateTransients(newState, currentTimeMs)

            // 1. Spawning
            newState = handleSpawning(newState, currentTimeMs)

            // 2. Enemy Movement
            val (movedState, updatedEnemies) = handleEnemyMovement(newState, currentTimeMs)
            newState = movedState.copy(enemies = updatedEnemies)

            // 3. Stall Firing
            newState = handleStallFiring(newState, currentTimeMs)

            // 4. Projectile Movement and Collision
            newState = handleProjectiles(newState, currentTimeMs)

            // 5. Wave completion check
            if (newState.waveActive && newState.enemiesToSpawn == 0 && newState.enemies.isEmpty()) {
                newState = newState.copy(waveActive = false, isBossWave = false)
                gameStateRepository.saveGameState(newState)
            }

            // 6. Game over check
            if (newState.health <= 0 && state.health > 0) {
                handleGameOver(newState)
            }

            newState
        }
    }

    private fun updateTransients(state: GameState, currentTimeMs: Long): GameState {
        val updatedPuddles = state.puddles.filter { currentTimeMs - it.spawnTimeMs < it.durationMs }
        val updatedVisualEffects = state.visualEffects.filter { currentTimeMs - it.startTimeMs < it.durationMs }
        return state.copy(puddles = updatedPuddles, visualEffects = updatedVisualEffects)
    }

    private fun handleSpawning(state: GameState, currentTimeMs: Long): GameState {
        if (state.waveActive && state.enemiesToSpawn > 0 && currentTimeMs - state.lastSpawnTimeMs > GameConstants.ENEMY_SPAWN_INTERVAL_MS && state.hexes.isNotEmpty() && state.enemiesToSpawnList.isNotEmpty()) {
            val startPos = state.startPosition ?: return state
            val endPos = state.endPosition ?: return state
            val path = Pathfinding.findPath(
                startPos, endPos, getBlockedCoordinates(state.hexes), state.hexes.keys
            ) ?: emptyList()

            val type = state.enemiesToSpawnList.first()
            val remainingSpawnList = state.enemiesToSpawnList.drop(1)

            val firstTarget = path.getOrNull(1) ?: startPos
            val isFacingLeft = firstTarget.q + firstTarget.r / 2f < startPos.q + startPos.r / 2f

            // Use EnemyBehaviorHandler to create enemy instance
            val enemyConfig = EnemyData.configs[type] ?: return state // Should not happen
            val newEnemy = EnemyBehaviorHandler.createEnemyInstance(
                enemyConfig,
                wave = state.currentWave,
                position = PreciseAxialCoordinate(startPos.q.toFloat(), startPos.r.toFloat()),
                path = path,
                isFacingLeft = isFacingLeft
            )
            return state.copy(
                enemies = state.enemies + newEnemy,
                enemiesToSpawn = state.enemiesToSpawn - 1,
                enemiesToSpawnList = remainingSpawnList,
                lastSpawnTimeMs = currentTimeMs
            )
        }
        return state
    }

    /**
     * Handles movement for all active enemies and applies puddle effects.
     *
     * @param state Current game state.
     * @param currentTimeMs Current game time.
     * @return Updated state and list of enemies.
     */
    private fun handleEnemyMovement(state: GameState, currentTimeMs: Long): Pair<GameState, List<Enemy>> {
        var mutableState = state
        val affectingStalls = mutableMapOf<Pair<AxialCoordinate, String>, MutableSet<String>>()

        val updatedEnemies = state.enemies.mapNotNull { enemy ->
            if (enemy.isDead) return@mapNotNull null

            // Use EnemyData to get config
            val enemyConfig = EnemyData.configs[enemy.type] ?: return@mapNotNull null // Should not happen

            // Logic extracted to EnemyBehaviorHandler
            var updatedEnemy = EnemyBehaviorHandler.updateSpecialBehavior(enemyConfig, enemy, currentTimeMs)

            var freezeDuration = updatedEnemy.freezeDurationMs
            if (freezeDuration > 0) {
                freezeDuration = Math.max(0, freezeDuration - GameConstants.GAME_TICK_DURATION_MS) // Use constant
            }

            var speedBoostDuration = updatedEnemy.speedBoostDurationMs
            if (speedBoostDuration > 0) {
                speedBoostDuration = Math.max(0, speedBoostDuration - GameConstants.GAME_TICK_DURATION_MS) // Use constant
            }

            var isStopped = updatedEnemy.isStopped
            var stopDurationMs = updatedEnemy.stopDurationMs
            var lastStopMs = updatedEnemy.lastStopMs

            state.puddles.forEach { puddle ->
                if (axialDistance(enemy.position, puddle.position) < GameConstants.PUDDLE_EFFECT_RADIUS_THRESHOLD && // Use constant
                    puddle.sourceStallCoord != null &&
                    puddle.sourceStallId != null
                ) {
                    affectingStalls
                        .getOrPut(puddle.sourceStallCoord to puddle.sourceStallId) { mutableSetOf() }
                        .add(enemy.id)
                }
            }
            if (isStopped || freezeDuration > 0) {
                return@mapNotNull enemy.copy(
                    isStopped = isStopped,
                    stopDurationMs = stopDurationMs,
                    lastStopMs = lastStopMs,
                    freezeDurationMs = freezeDuration,
                    speedBoostDurationMs = speedBoostDuration
                )
            }

            var speedMultiplier = 1.0f
            state.puddles.forEach { puddle ->
                if (axialDistance(enemy.position, puddle.position) < GameConstants.PUDDLE_EFFECT_RADIUS_THRESHOLD) { // Use constant
                    // Use EnemyBehaviorHandler for slow multiplier
                    speedMultiplier = EnemyBehaviorHandler.getPuddleSlowMultiplier(enemy.type)
                }
            }

            if (speedBoostDuration > 0) {
                speedMultiplier *= GameConstants.SPEED_BOOST_MULTIPLIER // Use constant
            }

            val effectiveSpeed = enemy.baseSpeed * speedMultiplier

            val targetIndex = enemy.currentPathIndex + 1
            if (targetIndex >= enemy.path.size) {
                mutableState = mutableState.copy(health = Math.max(0, mutableState.health - 1))
                return@mapNotNull null
            }

            val target = enemy.path[targetIndex]
            val dq = target.q - enemy.position.q
            val dr = target.r - enemy.position.r
            val dist = axialDistance(enemy.position, PreciseAxialCoordinate(target.q.toFloat(), target.r.toFloat()))

            val newIsFacingLeft = if (target.q + target.r / 2f != enemy.position.q + enemy.position.r / 2f) {
                target.q + target.r / 2f < enemy.position.q + enemy.position.r / 2f
            } else {
                enemy.isFacingLeft
            }

            if (dist < effectiveSpeed) {
                enemy.copy(
                    position = PreciseAxialCoordinate(target.q.toFloat(), target.r.toFloat()),
                    currentPathIndex = targetIndex,
                    currentSpeed = effectiveSpeed,
                    isStopped = isStopped,
                    stopDurationMs = stopDurationMs,
                    lastStopMs = lastStopMs,
                    freezeDurationMs = freezeDuration,
                    speedBoostDurationMs = speedBoostDuration,
                    animationTimeMs = enemy.animationTimeMs + GameConstants.GAME_TICK_DURATION_MS, // Use constant
                    isFacingLeft = newIsFacingLeft
                )
            } else {
                enemy.copy(
                    position = PreciseAxialCoordinate(
                        enemy.position.q + (dq / dist) * effectiveSpeed,
                        enemy.position.r + (dr / dist) * effectiveSpeed
                    ),
                    currentSpeed = effectiveSpeed,
                    isStopped = isStopped,
                    stopDurationMs = stopDurationMs,
                    lastStopMs = lastStopMs,
                    freezeDurationMs = freezeDuration,
                    speedBoostDurationMs = speedBoostDuration,
                    animationTimeMs = enemy.animationTimeMs + GameConstants.GAME_TICK_DURATION_MS, // Use constant
                    isFacingLeft = newIsFacingLeft
                )
            }
        }

        if (affectingStalls.isNotEmpty()) {
            val updatedHexes = mutableState.hexes.toMutableMap()
            affectingStalls.forEach { (source, enemyIds) ->
                val (coord, stallId) = source
                updatedHexes[coord]?.stall?.let { stall ->
                    if (stall.id == stallId) {
                        val newTargetIds = stall.uniqueTargetIds + enemyIds
                        updatedHexes[coord] = updatedHexes[coord]!!.copy(stall = stall.copy(uniqueTargetIds = newTargetIds))
                    }
                }
            }
            mutableState = mutableState.copy(hexes = updatedHexes)
        }

        return Pair(mutableState, updatedEnemies)
    }

    /**
     * Checks all stalls to see if they are ready to fire and creates projectiles/puddles.
     *
     * @param state Current game state.
     * @param currentTimeMs Current game time.
     * @return Updated state with new projectiles/puddles.
     */
    private fun handleStallFiring(state: GameState, currentTimeMs: Long): GameState {
        val newProjectiles = state.projectiles.toMutableList()
        val newPuddles = state.puddles.toMutableList()
        val updatedHexes = state.hexes.toMutableMap()

        for ((coord, tile) in state.hexes) {
            val stall = tile.stall
            if (stall != null && currentTimeMs - stall.lastFiredMs >= stall.fireRateMs) {
                val stallPos = PreciseAxialCoordinate(coord.q.toFloat(), coord.r.toFloat())
                val potentialTargets = state.enemies.filter { enemy ->
                    axialDistance(enemy.position, stallPos) <= stall.range
                }

                val target = when (stall.targetMode) {
                    TargetMode.FIRST -> potentialTargets.maxByOrNull { it.currentPathIndex }
                    TargetMode.CLOSEST -> potentialTargets.minByOrNull { axialDistance(it.position, stallPos) }
                    TargetMode.STRONGEST -> potentialTargets.maxByOrNull { it.health }
                    TargetMode.WEAKEST -> potentialTargets.minByOrNull { it.health }
                }

                if (target != null) {
                    // Use StallData to get config
                    val stallConfig = StallData.configs[stall.stallType] ?: continue // Should not happen
                    // Use StallActionHandler.fire
                    val fireResult = StallActionHandler.fire(stallConfig, stall, coord, target, currentTimeMs)
                    var updatedStall = (fireResult as? FireResult.NewProjectile)?.updatedStall ?: stall
                    updatedStall = updatedStall.copy(lastFiredMs = currentTimeMs)

                    when (fireResult) {
                        is FireResult.NewProjectile -> {
                            newProjectiles.add(fireResult.projectile)
                        }
                        is FireResult.NewPuddle -> {
                            newPuddles.add(fireResult.puddle)
                        }
                    }
                    updatedHexes[coord] = tile.copy(stall = updatedStall)
                }
            }
        }
        return state.copy(hexes = updatedHexes, projectiles = newProjectiles, puddles = newPuddles)
    }

    /**
     * Updates projectile positions and handles impacts with enemies.
     * Also attributes hits and kills to the source stalls.
     *
     * @param state Current game state.
     * @param currentTimeMs Current game time.
     * @return Updated state after projectile processing.
     */
    private fun handleProjectiles(state: GameState, currentTimeMs: Long): GameState {
        val finalProjectiles = mutableListOf<Projectile>()
        val hitEnemiesDetails = mutableMapOf<String, MutableList<Projectile>>()
        val newVisualEffects = state.visualEffects.toMutableList()

        state.projectiles.forEach { proj ->
            val targetPos = if (proj.targetEnemyId != null) {
                state.enemies.find { it.id == proj.targetEnemyId }?.position ?: proj.targetPosition
            } else {
                proj.targetPosition
            }

            val dq = targetPos.q - proj.position.q
            val dr = targetPos.r - proj.position.r
            val dist = axialDistance(proj.position, targetPos)

            if (dist < proj.speed) {
                // Visual Effect
                if (proj.aoeRadius > 0 && proj.sourceStallType != null) {
                    // Use StallData to get config for visual effect properties
                    val stallConfig = StallData.configs[proj.sourceStallType] ?: return@forEach // Should not happen
                    newVisualEffects.add(VisualEffect(
                        id = UUID.randomUUID().toString(),
                        position = targetPos,
                        color = stallConfig.visualEffectColor ?: proj.color.copy(alpha = 0.5f),
                        startTimeMs = currentTimeMs,
                        durationMs = stallConfig.visualEffectDuration,
                        type = stallConfig.visualEffectType
                    ))
                }

                // Collect hits
                state.enemies.forEach { enemy ->
                    val isDirectTarget = proj.targetEnemyId == enemy.id
                    val isWithinAoe = proj.aoeRadius > 0 && axialDistance(enemy.position, targetPos) <= proj.aoeRadius
                    if (isDirectTarget || isWithinAoe) {
                        hitEnemiesDetails.getOrPut(enemy.id) { mutableListOf() }.add(proj)
                    }
                }
            } else {
                // Keep moving
                finalProjectiles.add(proj.copy(
                    lastPosition = proj.position,
                    position = PreciseAxialCoordinate(
                        proj.position.q + (dq / dist) * proj.speed,
                        proj.position.r + (dr / dist) * proj.speed
                    )
                ))
            }
        }

        var updatedGold = state.gold
        var updatedScore = state.score
        val updatedHexes = state.hexes.toMutableMap()

        val finalEnemies = state.enemies.map { enemy ->
            val hits = hitEnemiesDetails[enemy.id]
            if (hits != null) {
                var currentHealth = enemy.health
                var maxFreezeDuration = enemy.freezeDurationMs
                var speedBoostDuration = enemy.speedBoostDurationMs

                hits.forEach { proj ->
                    if (currentHealth <= 0) return@forEach

                    var damage = proj.damage.toFloat()
                    var freezeDuration = proj.freezeDurationMs

                    // Apply modifiers
                    if (proj.sourceStallType != null) {
                        // Use StallData to get config for modifiers
                        val stallConfig = StallData.configs[proj.sourceStallType] ?: return@forEach // Should not happen
                        // Use StallActionHandler for modifiers
                        damage = StallActionHandler.applyDamageModifiers(stallConfig, enemy, damage)
                        freezeDuration = StallActionHandler.getFreezeModifier(stallConfig, enemy, freezeDuration)
                        val boost = StallActionHandler.getSpeedBoost(stallConfig, enemy)
                        if (boost > 0) speedBoostDuration = boost
                    }

                    val damageDealt = damage.toInt()
                    currentHealth = Math.max(0, currentHealth - damageDealt)
                    maxFreezeDuration = Math.max(maxFreezeDuration, freezeDuration)

                    // Track hit and kill
                    if (proj.sourceStallCoord != null && proj.sourceStallId != null) {
                        val coord = proj.sourceStallCoord
                        updatedHexes[coord]?.stall?.let { stall ->
                            if (stall.id == proj.sourceStallId) {
                                val isKill = currentHealth <= 0
                                val newTargetIds = stall.uniqueTargetIds + enemy.id
                                val newKills = if (isKill) stall.kills + 1 else stall.kills
                                updatedHexes[coord] = updatedHexes[coord]!!.copy(stall = stall.copy(
                                    uniqueTargetIds = newTargetIds,
                                    kills = newKills
                                ))
                            }
                        }
                    }
                }

                if (currentHealth <= 0) {
                    updatedGold += enemy.reward
                    updatedScore += enemy.reward
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastHapticTimeMs >= 1000) {
                        viewModelScope.launch {
                            if (settingsRepository.settingsFlow.first().hapticEnabled) _hapticEvents.emit(Unit)
                        }
                        lastHapticTimeMs = currentTime
                    }
                    enemy.copy(health = 0, isDead = true)
                } else {
                    enemy.copy(health = currentHealth, freezeDurationMs = maxFreezeDuration, speedBoostDurationMs = speedBoostDuration)
                }
            } else enemy
        }.filter { !it.isDead }

        return state.copy(
            hexes = updatedHexes,
            enemies = finalEnemies,
            projectiles = finalProjectiles,
            visualEffects = newVisualEffects,
            gold = updatedGold,
            score = updatedScore
        )
    }

    private fun handleGameOver(state: GameState) {
        val finalScore = state.score
        val finalWave = state.currentWave
        val date = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        val newHighScore = HighScore(finalScore, finalWave, date)

        viewModelScope.launch {
            settingsRepository.updateSettings { currentSettings ->
                val updatedScores = (currentSettings.highScores + newHighScore)
                    .sortedByDescending { it.score }
                    .take(5)
                currentSettings.copy(highScores = updatedScores)
            }
            gameStateRepository.deleteGameState()
        }
    }

    private fun axialDistance(a: PreciseAxialCoordinate, b: PreciseAxialCoordinate): Float {
        return GridUtils.axialDistance(a, b)
    }

    fun onCellClick(coord: AxialCoordinate) {
        val currentState = _gameState.value
        val tile = currentState.hexes[coord] ?: return

        if (tile.stall != null) {
            // Select existing stall
            _gameState.update { it.copy(selectedBoardStall = coord, selectedStallType = null) }
        } else if (currentState.selectedStallType != null) {
            // Place new stall
            val stallToPlace = currentState.selectedStallType
            if (currentState.gold >= stallToPlace.cost && tile.type == TileType.FLOOR && tile.stall == null) {
                val blocked = getBlockedCoordinates(currentState.hexes) + coord
                val startPos = currentState.startPosition ?: return
                val endPos = currentState.endPosition ?: return

                val startPath = Pathfinding.findPath(startPos, endPos, blocked, currentState.hexes.keys)

                if (startPath != null) {
                    val canRepathAll = currentState.enemies.all { enemy ->
                        val currentTarget = enemy.path.getOrNull(enemy.currentPathIndex + 1) ?: endPos
                        Pathfinding.findPath(currentTarget, endPos, blocked, currentState.hexes.keys) != null
                    }

                    if (canRepathAll) {
                        val newHexes = currentState.hexes.toMutableMap()
                        // Use StallActionHandler to create stall instance
                        val stallConfig = StallData.configs[stallToPlace.stallType] ?: return
                        newHexes[coord] = tile.copy(stall = StallActionHandler.createStallInstance(stallConfig))

                        val updatedEnemies = recalculateEnemyPaths(currentState, blocked, newHexes)
                        _gameState.update { it.copy(hexes = newHexes, gold = it.gold - stallToPlace.cost, enemies = updatedEnemies) }
                        return
                    }
                }
            }
        } else {
            // Deselect
            _gameState.update { it.copy(selectedBoardStall = null, selectedStallType = null) }
        }
    }

    fun sellStall() {
        val currentState = _gameState.value
        val coord = currentState.selectedBoardStall ?: return
        val tile = currentState.hexes[coord] ?: return
        val stall = tile.stall ?: return

        val refund = (stall.totalInvestment * StallConstants.SELL_REFUND_PERCENTAGE).toInt() // Use constant
        val newHexes = currentState.hexes.toMutableMap()
        newHexes[coord] = tile.copy(stall = null)

        val blocked = getBlockedCoordinates(newHexes)

        _gameState.update { state ->
            val updatedEnemies = recalculateEnemyPaths(state, blocked, newHexes)
            state.copy(
                hexes = newHexes,
                gold = state.gold + refund,
                enemies = updatedEnemies,
                selectedBoardStall = null
            )
        }
    }

    fun upgradeStall() {
        _gameState.update { state ->
            val coord = state.selectedBoardStall ?: return@update state
            val tile = state.hexes[coord] ?: return@update state
            val stall = tile.stall ?: return@update state

            // Use StallData to get config for base stats
            val stallConfig = StallData.configs[stall.stallType] ?: return@update state // Should not happen
            // Get base stall for upgrade calculations (this might need refinement if base stats differ)
            // For now, we assume stallConfig provides the base stats needed for getUpgradeCost
            val baseStallForUpgradeCalc = stallConfig // This might need to be a specific StallDefinition if logic differs

            // Use StallActionHandler for upgrade cost and benefits
            // NOTE: stall.getUpgradeCost() is still called on the Stall instance. If this logic needs to be externalized, it should be refactored.
            val upgradeCost = stall.getUpgradeCost() // This needs stall.totalInvestment if getUpgradeCost uses it

            if (state.gold >= upgradeCost) {
                val upgradeCategories = mutableListOf(
                    StallUpgradeCategory.DAMAGE.ordinal,
                    StallUpgradeCategory.RATE.ordinal,
                    StallUpgradeCategory.SPECIAL.ordinal
                ).apply { shuffle() }
                // Use StallActionHandler.getUpgradeBenefit for calculation.
                // The logic for getting the base stall for upgrade benefit calculation needs to be consistent.

                while (upgradeCategories.isNotEmpty()) {
                    val upgradeTypeIndex = upgradeCategories.removeAt(0)
                    val mutableUpgrades = stall.upgrades.toMutableMap()

                    var newDamage = stall.damage
                    var newRange = stall.range
                    var newFireRate = stall.fireRateMs
                    var newAoeRadius = stall.aoeRadius
                    var newEffectDuration = stall.effectDurationMs
                    var newFreezeDuration = stall.freezeDurationMs
                    var currentCategoryName = ""

                    when (upgradeTypeIndex) {
                        StallUpgradeCategory.DAMAGE.ordinal -> { // Damage or Range
                            if (kotlin.random.Random.nextBoolean()) {
                                currentCategoryName = "Damage"
                                // Use StallActionHandler.getUpgradeDamageIncrease
                                val damageIncrease = StallActionHandler.getUpgradeDamageIncrease(baseStallForUpgradeCalc.damage)
                                newDamage += damageIncrease
                                val newLevel = mutableUpgrades.getOrDefault("Damage", 0) + 1
                                if (newLevel % StallConstants.LEGENDARY_NAME_LEVEL_THRESHOLD == 0) { // Use constant
                                    newDamage = Math.round(newDamage * 1.25f)
                                }
                                mutableUpgrades["Damage"] = newLevel
                            } else {
                                currentCategoryName = "Range"
                                newRange += 0.5f
                                val newLevel = mutableUpgrades.getOrDefault("Range", 0) + 1
                                if (newLevel % StallConstants.LEGENDARY_NAME_LEVEL_THRESHOLD == 0) { // Use constant
                                    newRange *= 1.25f
                                }
                                mutableUpgrades["Range"] = newLevel
                            }
                        }
                        StallUpgradeCategory.RATE.ordinal -> { // Rate
                            currentCategoryName = "Rate"
                            val rateReduction = (baseStallForUpgradeCalc.fireRateMs * 0.1f).toLong()
                            var potentialRate = stall.fireRateMs - rateReduction
                            val newLevel = mutableUpgrades.getOrDefault("Rate", 0) + 1
                            if (newLevel % StallConstants.LEGENDARY_NAME_LEVEL_THRESHOLD == 0) { // Use constant
                                potentialRate = Math.round(potentialRate * 0.75)
                            }

                            if (potentialRate < StallConstants.MIN_FIRE_RATE_MS) { // Minimum fire rate constant
                                continue // Try another category
                            }

                            newFireRate = potentialRate
                            mutableUpgrades["Rate"] = newLevel
                        }
                        StallUpgradeCategory.SPECIAL.ordinal -> { // Radius, Duration, Effect, or Damage (based on stall type)
                            when (stall.stallType) {
                                StallType.SATAY, StallType.DURIAN -> {
                                    currentCategoryName = "Radius"
                                    newAoeRadius += 0.2f
                                    val newLevel = mutableUpgrades.getOrDefault("Radius", 0) + 1
                                    if (newLevel % StallConstants.LEGENDARY_NAME_LEVEL_THRESHOLD == 0) { // Use constant
                                        newAoeRadius *= 1.25f
                                    }
                                    mutableUpgrades["Radius"] = newLevel
                                }
                                StallType.TEH_TARIK -> {
                                    currentCategoryName = "Duration"
                                    newEffectDuration += 500L
                                    val newLevel = mutableUpgrades.getOrDefault("Duration", 0) + 1
                                    if (newLevel % StallConstants.LEGENDARY_NAME_LEVEL_THRESHOLD == 0) { // Use constant
                                        newEffectDuration = Math.round(newEffectDuration * 1.25)
                                    }
                                    mutableUpgrades["Duration"] = newLevel
                                }
                                StallType.ICE_KACHANG -> {
                                    currentCategoryName = "Effect"
                                    newFreezeDuration += 100L
                                    val newLevel = mutableUpgrades.getOrDefault("Effect", 0) + 1
                                    if (newLevel % StallConstants.LEGENDARY_NAME_LEVEL_THRESHOLD == 0) { // Use constant
                                        newFreezeDuration = Math.round(newFreezeDuration * 1.25)
                                    }
                                    mutableUpgrades["Effect"] = newLevel
                                }
                                StallType.CHICKEN_RICE -> {
                                    currentCategoryName = "Damage"
                                    // Use StallActionHandler for damage calculation
                                    val damageIncrease = StallActionHandler.getUpgradeDamageIncrease(baseStallForUpgradeCalc.damage)
                                    newDamage += damageIncrease
                                    val newLevel = mutableUpgrades.getOrDefault("Damage", 0) + 1
                                    if (newLevel % StallConstants.LEGENDARY_NAME_LEVEL_THRESHOLD == 0) { // Use constant
                                        newDamage = Math.round(newDamage * 1.25f)
                                    }
                                    mutableUpgrades["Damage"] = newLevel
                                }
                            }
                        }
                    }

                    var newPrefix = stall.legendaryPrefix
                    var newSuffix = stall.legendarySuffix
                    val newNamingCategories = stall.namingCategories.toMutableList()

                    val levelOfUpgradedCat = mutableUpgrades[currentCategoryName] ?: 0
                    if (levelOfUpgradedCat == 10 && !stall.namingCategories.contains(currentCategoryName)) {
                        if (stall.namingCategories.isEmpty()) {
                            // LegendaryNames class needs to be created or its logic assumed/implemented
                            newSuffix = LegendaryNames.getRandomSuffix(currentCategoryName)
                            newNamingCategories.add(currentCategoryName)
                        } else if (stall.namingCategories.size == 1) {
                            // LegendaryNames class needs to be created or its logic assumed/implemented
                            newPrefix = LegendaryNames.getRandomPrefix(currentCategoryName)
                            newNamingCategories.add(currentCategoryName)
                        }
                    }

                    val newName = LegendaryNames.constructName(stall.baseName, newPrefix, newSuffix)

                    val updatedStall = stall.copy(
                        name = newName,
                        damage = newDamage,
                        range = newRange,
                        fireRateMs = newFireRate,
                        aoeRadius = newAoeRadius,
                        effectDurationMs = newEffectDuration,
                        freezeDurationMs = newFreezeDuration,
                        upgradeCount = stall.upgradeCount + 1,
                        totalInvestment = stall.totalInvestment + upgradeCost,
                        upgrades = mutableUpgrades,
                        legendaryPrefix = newPrefix,
                        legendarySuffix = newSuffix,
                        namingCategories = newNamingCategories
                    )

                    val newHexes = state.hexes.toMutableMap()
                    newHexes[coord] = tile.copy(stall = updatedStall)
                    return@update state.copy(hexes = newHexes, gold = state.gold - upgradeCost)
                }
            }
            state
        }
    }

    fun cycleTargetMode() {
        val currentState = _gameState.value
        val coord = currentState.selectedBoardStall ?: return
        val tile = currentState.hexes[coord] ?: return
        val stall = tile.stall ?: return

        val modes = TargetMode.values()
        val nextMode = modes[(stall.targetMode.ordinal + 1) % modes.size]

        val newHexes = currentState.hexes.toMutableMap()
        newHexes[coord] = tile.copy(stall = stall.copy(targetMode = nextMode))
        _gameState.update { it.copy(hexes = newHexes) }
    }

    private fun recalculateEnemyPaths(
        state: GameState,
        blocked: Set<AxialCoordinate>,
        hexes: Map<AxialCoordinate, HexTile>
    ): List<Enemy> {
        val endPos = state.endPosition ?: return state.enemies
        return state.enemies.map { enemy ->
            val currentTargetIndex = enemy.currentPathIndex + 1
            if (currentTargetIndex >= enemy.path.size) return@map enemy

            val currentTarget = enemy.path[currentTargetIndex]
            val newPathToFollow = Pathfinding.findPath(
                currentTarget, endPos, blocked, hexes.keys
            ) ?: listOf(currentTarget)

            val newPath = enemy.path.subList(0, currentTargetIndex + 1) + newPathToFollow.drop(1)

            val nextTarget = newPath.getOrNull(currentTargetIndex + 1) ?: currentTarget
            val newIsFacingLeft = if (nextTarget.q + nextTarget.r / 2f != enemy.position.q + enemy.position.r / 2f) {
                nextTarget.q + nextTarget.r / 2f < enemy.position.q + enemy.position.r / 2f
            } else {
                enemy.isFacingLeft
            }

            enemy.copy(path = newPath, isFacingLeft = newIsFacingLeft)
        }
    }

    private fun getBlockedCoordinates(hexes: Map<AxialCoordinate, HexTile>): Set<AxialCoordinate> {
        return hexes.values.filter {
            it.stall != null || it.type == TileType.PILLAR || it.type == TileType.GOAL_TABLE || it.type.name.startsWith("EDGE_")
        }.map { it.coordinate }.toSet()
    }
}
