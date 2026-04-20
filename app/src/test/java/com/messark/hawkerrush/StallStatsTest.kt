package com.messark.hawkerrush

import android.app.Application
import androidx.compose.ui.graphics.Color
import com.messark.hawkerrush.model.*
import com.messark.hawkerrush.utils.GameStateRepository
import com.messark.hawkerrush.utils.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
class StallStatsTest {
    private val testDispatcher = StandardTestDispatcher()

import kotlinx.coroutines.test.resetMain
import org.junit.After

class StallStatsTest {
    private val testDispatcher = StandardTestDispatcher()

    `@Before`
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    `@After`
    fun tearDown() {
        Dispatchers.resetMain()
    }
}

    @Test
    fun `stalls track unique hits and kills`() {
        val application = mockk<Application>(relaxed = true)
        val settingsRepository = mockk<SettingsRepository>()
        val gameStateRepository = mockk<GameStateRepository>(relaxed = true)
        every { settingsRepository.settingsFlow } returns kotlinx.coroutines.flow.flowOf(Settings())

        val viewModel = MainViewModel(application, settingsRepository, gameStateRepository)

        // Initial state with one stall and one enemy
        val stallCoord = AxialCoordinate(0, 0)
        val enemyId = "enemy1"
        val stall = Stall(
            id = "s1",
            name = "Chicken Rice",
            cost = 100,
            color = Color.Yellow,
            stallType = StallType.CHICKEN_RICE,
            damage = 50
        )

        val enemy = Enemy(
            id = enemyId,
            health = 100,
            maxHealth = 100,
            position = PreciseAxialCoordinate(1f, 0f), // within range (range is 4f)
            path = listOf(AxialCoordinate(0, 0), AxialCoordinate(1, 0), AxialCoordinate(2, 0))
        )

        // We use a private method via reflection or just trigger the game loop?
        // Let's use reflection to call updateGame if it's private, but wait,
        // handleProjectiles is what we really want to test.

        // Let's manually construct a state and call handleProjectiles
        val state = GameState(
            hexes = mapOf(stallCoord to HexTile(stallCoord, TileType.FLOOR, stall)),
            enemies = listOf(enemy),
            projectiles = listOf(
                Projectile(
                    id = "p1",
                    position = PreciseAxialCoordinate(1f, 0f), // already at enemy
                    targetEnemyId = enemyId,
                    targetPosition = PreciseAxialCoordinate(1f, 0f),
                    damage = 50,
                    color = Color.Yellow,
                    sourceStallCoord = stallCoord
                )
            )
        )

        // Access handleProjectiles using reflection since it's private
        val method = MainViewModel::class.java.getDeclaredMethod("handleProjectiles", GameState::class.java, Long::class.java)
        method.isAccessible = true

        // 1. First hit
        var newState = method.invoke(viewModel, state, 1000L) as GameState
        var updatedStall = newState.hexes[stallCoord]?.stall
        assertEquals(1, updatedStall?.uniqueTargetIds?.size)
        assertEquals(0, updatedStall?.kills)
        assertEquals(50, newState.enemies[0].health)

        // 2. Second hit on SAME enemy (should NOT increment unique hits, but should kill and increment kills)
        val state2 = newState.copy(
            projectiles = listOf(
                Projectile(
                    id = "p2",
                    position = PreciseAxialCoordinate(1f, 0f),
                    targetEnemyId = enemyId,
                    targetPosition = PreciseAxialCoordinate(1f, 0f),
                    damage = 50,
                    color = Color.Yellow,
                    sourceStallCoord = stallCoord
                )
            )
        )

        newState = method.invoke(viewModel, state2, 1100L) as GameState
        updatedStall = newState.hexes[stallCoord]?.stall
        assertEquals(1, updatedStall?.uniqueTargetIds?.size) // Still 1
        assertEquals(1, updatedStall?.kills) // Now 1
        assertEquals(0, newState.enemies.size) // Dead
    }
}
