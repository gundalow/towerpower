
    fun sellStall() {
        val currentState = _gameState.value
        val coord = currentState.selectedBoardStall ?: return
        val tile = currentState.hexes[coord] ?: return
        val stall = tile.stall ?: return

        // Calculate refund (50% of total investment)
        val refund = (stall.totalInvestment * 0.5f).toInt()

        // Create a new HexTile with stall removed
        val newHexes = currentState.hexes.toMutableMap()
        newHexes[coord] = tile.copy(stall = null)

        // Recalculate enemy paths as the board has changed
        val blocked = getBlockedCoordinates(newHexes)
        val updatedEnemies = recalculateEnemyPaths(currentState, blocked, newHexes)

        _gameState.update { state ->
            state.copy(
                hexes = newHexes,
                gold = state.gold + refund,
                enemies = updatedEnemies,
                selectedBoardStall = null // Deselect the stall after selling
            )
        }
    }
