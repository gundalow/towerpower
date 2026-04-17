# Codebase Review - Hawker Rush

## 1. Potential Bugs, Edge-Cases, and Race-Conditions

### 1.1 Gold-Check Race Condition
*   **What**: In `MainViewModel.kt`, functions like `onCellClick` and `upgradeStall` check if the player has enough gold *before* entering the `_gameState.update` block.
*   **Why**: Between the check and the actual update, another coroutine or the game loop could have updated the state (e.g., another purchase or a refund), leading to a race condition where gold could potentially drop below zero.
*   **How**: Move the gold validation logic *inside* the `update` block to ensure atomicity.

```kotlin
// Example fix for onCellClick in MainViewModel.kt
<<<<<<< SEARCH
        } else if (currentState.selectedStallType != null) {
            // Place new stall
            val stallToPlace = currentState.selectedStallType
            if (currentState.gold >= stallToPlace.cost && tile.type == TileType.FLOOR && tile.stall == null) {
                val blocked = getBlockedCoordinates(currentState.hexes) + coord
=======
        } else if (currentState.selectedStallType != null) {
            _gameState.update { state ->
                val stallToPlace = state.selectedStallType ?: return@update state
                val tile = state.hexes[coord] ?: return@update state
                if (state.gold >= stallToPlace.cost && tile.type == TileType.FLOOR && tile.stall == null) {
                    val blocked = getBlockedCoordinates(state.hexes) + coord
>>>>>>> REPLACE
```

### 1.2 Tourist Photography Pause during Freeze
*   **What**: The game loop in `MainViewModel.kt` returns early if an enemy's `freezeDurationMs > 0`.
*   **Why**: This prevents the `stopDurationMs` (the Tourist's photography timer) from decrementing while the enemy is frozen. As a result, the Tourist is effectively "paused" in their paused state, extending their stationary time unfairly.
*   **How**: Update the freeze duration and the stop duration in the same tick instead of returning early.

```kotlin
// Example fix in MainViewModel.kt
<<<<<<< SEARCH
                if (enemy.freezeDurationMs > 0) {
                    return@mapNotNull enemy.copy(freezeDurationMs = Math.max(0, enemy.freezeDurationMs - 32))
                }

                var isStopped = enemy.isStopped
=======
                val newFreezeDuration = Math.max(0, enemy.freezeDurationMs - 32)

                var isStopped = enemy.isStopped
>>>>>>> REPLACE
```

### 1.3 Satay Stall "Chili Sauce" Cone Implementation
*   **What**: The Satay stall is described as having "Area chili sauce damage" and it calculates a `rotation`. However, the damage is applied via a standard circular `aoeRadius` at the target's position.
*   **Why**: The "chili sauce" description and the rotation logic imply a directional cone attack, but the implementation just uses a generic circular AOE at the impact point.
*   **How**: Implement a cone-based check in the collision logic.

```kotlin
// Example fix in MainViewModel.kt (Collision Logic)
<<<<<<< SEARCH
                    } else {
                        newState.enemies.forEach { enemy ->
                            if (axialDistance(enemy.position, proj.targetPosition) <= proj.aoeRadius) {
                                hitEnemies[enemy.id] = hitEnemies.getOrDefault(enemy.id, 0) + proj.damage
=======
                    } else {
                        newState.enemies.forEach { enemy ->
                            val dist = axialDistance(enemy.position, proj.targetPosition)
                            val isHit = if (proj.isArc && proj.startPosition != null) {
                                // Cone logic: check distance from start and angle
                                val angleToEnemy = Math.atan2((enemy.position.r - proj.startPosition.r).toDouble(), (enemy.position.q - proj.startPosition.q).toDouble())
                                val angleToTarget = Math.atan2((proj.targetPosition.r - proj.startPosition.r).toDouble(), (proj.targetPosition.q - proj.startPosition.q).toDouble())
                                val angleDiff = Math.abs(angleToEnemy - angleToTarget)
                                angleDiff < 0.5 && axialDistance(enemy.position, proj.startPosition) <= axialDistance(proj.targetPosition, proj.startPosition) + 0.5f
                            } else {
                                dist <= proj.aoeRadius
                            }
                            if (isHit) {
                                hitEnemies[enemy.id] = hitEnemies.getOrDefault(enemy.id, 0) + proj.damage
>>>>>>> REPLACE
```

---

## 2. Code Structure Implementations

### 2.1 Game State Persistence Gaps
*   **What**: `GameStateRepository.kt` and `PersistentGameState` only save high-level stats (gold, health, hexes, wave).
*   **Why**: Active enemies, projectiles, and puddles are lost if the app is closed. Furthermore, `waveActive` is forced to `false` on load, making it impossible to resume a wave in progress.
*   **How**: Expand `PersistentGameState` to include serialized lists of `Enemy`, `Projectile`, and `StickyPuddle`.

```kotlin
// Example fix in GameStateRepository.kt
<<<<<<< SEARCH
data class PersistentGameState(
    val health: Int,
    val gold: Int,
    val hexes: List<HexTile>,
    val startPosition: AxialCoordinate?,
    val endPosition: AxialCoordinate?,
    val currentWave: Int,
    val score: Int
)
=======
data class PersistentGameState(
    val health: Int,
    val gold: Int,
    val hexes: List<HexTile>,
    val startPosition: AxialCoordinate?,
    val endPosition: AxialCoordinate?,
    val currentWave: Int,
    val score: Int,
    val enemies: List<Enemy>,
    val waveActive: Boolean
)
>>>>>>> REPLACE
```

### 2.2 Hardcoded UI Constants in GameBoard
*   **What**: `GameBoard.kt` uses several hardcoded values for rendering (e.g., `47.dp` hex width, `0.69f` row spacing).
*   **Why**: This makes the UI difficult to adjust for different screen sizes and breaks the separation of concerns between logic and layout configuration.
*   **How**: Move these constants to `LayoutConstants.kt`.

```kotlin
// Example fix in GameBoard.kt
<<<<<<< SEARCH
    val hexWidth = 47.dp // Reduced from 48.dp to bring hexes closer
    val hexHeight = hexWidth * 91f / 101f

    // Vertical spacing multiplier for pointy-top hexes.
    val rowSpacingFactor = 0.69f
=======
    val hexWidth = LayoutConstants.HEX_WIDTH
    val hexHeight = hexWidth * LayoutConstants.HEX_ASPECT_RATIO
    val rowSpacingFactor = LayoutConstants.ROW_SPACING_FACTOR
>>>>>>> REPLACE
```

### 2.3 Repathing Efficiency
*   **What**: When a stall is placed or sold, the game calls `Pathfinding.findPath` for every single active enemy.
*   **Why**: In late game waves with 50+ enemies, this can cause a noticeable "hiccup" or frame drop.
*   **How**: Use a Dijkstra map (distance field) calculated once from the goal.

```kotlin
// Example fix in Pathfinding.kt
<<<<<<< SEARCH
object Pathfinding {
    fun findPath(
        start: AxialCoordinate,
=======
object Pathfinding {
    fun generateDistanceField(
        goal: AxialCoordinate,
        blockedPositions: Set<AxialCoordinate>,
        allCoordinates: Set<AxialCoordinate>
    ): Map<AxialCoordinate, Int> {
        val distances = mutableMapOf(goal to 0)
        val queue: Queue<AxialCoordinate> = LinkedList(listOf(goal))
        while (queue.isNotEmpty()) {
            val current = queue.poll()!!
            val dist = distances[current]!!
            getNeighbors(current).forEach { neighbor ->
                if (neighbor in allCoordinates && neighbor !in blockedPositions && neighbor !in distances) {
                    distances[neighbor] = dist + 1
                    queue.add(neighbor)
                }
            }
        }
        return distances
    }

    fun findPath(
>>>>>>> REPLACE
```

---

## 3. Game Play Improvements

### 3.1 Persistence of Upgrades (Anti-Save-Scumming)
*   **What**: Upgrades provide random attribute boosts.
*   **Why**: Players can "save-scum" (exit and reload) to reroll bad upgrade results because the state isn't saved immediately.
*   **How**: Trigger a save call immediately after an upgrade.

```kotlin
// Example fix in MainViewModel.kt
<<<<<<< SEARCH
            newHexes[coord] = tile.copy(stall = updatedStall)
            _gameState.update { it.copy(hexes = newHexes, gold = it.gold - upgradeCost) }
=======
            newHexes[coord] = tile.copy(stall = updatedStall)
            _gameState.update {
                val newState = it.copy(hexes = newHexes, gold = it.gold - upgradeCost)
                gameStateRepository.saveGameState(newState)
                newState
            }
>>>>>>> REPLACE
```

### 3.2 Feedback for Invalid Placement
*   **What**: If a stall placement blocks the last available path, the action is silently ignored.
*   **Why**: Confusing for players who don't understand why they can't place a stall.
*   **How**: Add a UI event stream to notify the player.

```kotlin
// Example fix in MainViewModel.kt
<<<<<<< SEARCH
    private val _hapticEvents = MutableSharedFlow<Unit>()
    val hapticEvents: SharedFlow<Unit> = _hapticEvents.asSharedFlow()
=======
    private val _hapticEvents = MutableSharedFlow<Unit>()
    val hapticEvents: SharedFlow<Unit> = _hapticEvents.asSharedFlow()

    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()
>>>>>>> REPLACE

// And in onCellClick:
// else { viewModelScope.launch { _uiEvents.emit("Cannot block path!") } }
```

### 3.3 Chicken Rice Stall Balancing
*   **What**: Chicken Rice stalls are too cost-effective.
*   **Why**: Fast fire rate (700ms) and low cost (100g) make them over-powered compared to other stalls.
*   **How**: Increase base cost and slightly reduce fire rate.

```kotlin
// Example fix in MainViewModel.kt
<<<<<<< SEARCH
            Stall("t3", "Chicken Rice", 100, Color.Yellow, stallType = StallType.CHICKEN_RICE, range = 4f, damage = 15, fireRateMs = 700, description = "High single-target damage"),
=======
            Stall("t3", "Chicken Rice", 150, Color.Yellow, stallType = StallType.CHICKEN_RICE, range = 4f, damage = 15, fireRateMs = 850, description = "Efficient single-target damage"),
>>>>>>> REPLACE
```

---

## 4. Haptic Feedback Enhancements

### 4.1 "Unnoticeable" Haptic Feedback Triggers
*   **What**: Haptic feedback is implemented but often unnoticeable.
*   **Why**: Two factors contribute:
    1. A 1000ms debounce in `MainViewModel.kt` during enemy deaths means if multiple enemies die quickly, only the first one vibrates.
    2. The use of `HapticFeedbackType.LongPress` in `MainActivity.kt` is technically a very long, low-frequency buzz that doesn't feel like a "hit" or "death" impact.
*   **How**: Reduce the death debounce and switch to a more sharp feedback type like `TextHandleMove` or `ContextClick` (if available on the OS version) or just a standard vibrate.

```kotlin
// Fix 1: Reduce debounce in MainViewModel.kt
<<<<<<< SEARCH
                        if (currentTime - lastHapticTimeMs >= 1000) {
=======
                        if (currentTime - lastHapticTimeMs >= 150) {
>>>>>>> REPLACE

// Fix 2: Change feedback type in MainActivity.kt
<<<<<<< SEARCH
                    viewModel.hapticEvents.collect {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
=======
                    viewModel.hapticEvents.collect {
                        // Use a more distinct feedback for gameplay events
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
>>>>>>> REPLACE
```

---

## 5. Other Recommendations

### 5.1 Pathfinding Unit Tests
*   **What**: Lack of automated verification for core navigation logic.
*   **Why**: High risk of breaking game flow with map generation or stall logic changes.
*   **How**: Implement standard A* test cases.

```kotlin
// Example fix in PathfindingTest.kt
@Test
fun testPathfindingReturnsNullWhenBlocked() {
    val start = AxialCoordinate(0, 0)
    val end = AxialCoordinate(2, 2)
    val blocked = setOf(AxialCoordinate(1, 0), AxialCoordinate(0, 1), AxialCoordinate(1, 1))
    val all = setOf(start, end, AxialCoordinate(1, 0), AxialCoordinate(0, 1), AxialCoordinate(1, 1))
    assertNull(Pathfinding.findPath(start, end, blocked, all))
}
```

### 5.2 Externalized Sprite Data
*   **What**: Sprite coordinates are hardcoded in `SpriteConstants.kt`.
*   **Why**: Hard to maintain and update without recompilation.
*   **How**: Move to a JSON resource.

```kotlin
// Proposed JSON structure:
// { "FLOOR_01": { "l": 8, "t": 33, "r": 109, "b": 124 }, ... }
```
