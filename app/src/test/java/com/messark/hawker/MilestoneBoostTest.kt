package com.messark.hawker

import android.app.Application
import androidx.compose.ui.graphics.Color
import com.messark.hawker.model.*
import com.messark.hawker.utils.GameStateRepository
import com.messark.hawker.utils.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MilestoneBoostTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getUpgradeBenefit calculates cumulative boost correctly for Damage`() {
        val baseStall = Stall(
            id = "base",
            name = "Chicken Rice",
            cost = 100,
            color = Color.Yellow,
            stallType = StallType.CHICKEN_RICE,
            damage = 10
        )

        // Level 9: 10 + 9 * ( (10*0.3).toInt() + 2 ) = 10 + 9 * 5 = 55
        // Increase is 45. 45/10 = 4.5 -> 450%
        val benefit9 = baseStall.getUpgradeBenefit("Damage", 9)
        assertEquals("+450%", benefit9)

        // Level 10: (10 + 10 * 5) * 1.25 = 60 * 1.25 = 75
        // Increase is 75 - 10 = 65. 65/10 = 6.5 -> 650%
        val benefit10 = baseStall.getUpgradeBenefit("Damage", 10)
        assertEquals("+650%", benefit10)
    }

    @Test
    fun `upgradeStall applies 25 percent boost at level 10`() {
        val application = mockk<Application>(relaxed = true)
        val settingsRepository = mockk<SettingsRepository>()
        val gameStateRepository = mockk<GameStateRepository>(relaxed = true)
        every { settingsRepository.settingsFlow } returns kotlinx.coroutines.flow.flowOf(Settings())

        val viewModel = MainViewModel(application, settingsRepository, gameStateRepository)

        val stallCoord = AxialCoordinate(0, 0)
        // Base damage 10. Increase per level 5.
        // Damage at level 9 = 10 + 9 * 5 = 55.
        val stall = Stall(
            id = "s1",
            name = "Chicken Rice",
            cost = 100,
            color = Color.Yellow,
            stallType = StallType.CHICKEN_RICE,
            damage = 55,
            upgrades = mapOf("Damage" to 9),
            upgradeCount = 9
        )

        viewModel._gameState.value = GameState(
            hexes = mapOf(stallCoord to HexTile(stallCoord, TileType.FLOOR, stall)),
            gold = 10000,
            selectedBoardStall = stallCoord
        )

        // We might need to call it multiple times since category selection is random
        // but since we only care about Damage reaching 10, and it's 1/3 chance roughly.
        // Actually, let's just loop until Damage is upgraded to 10.
        var attempts = 0
        while (viewModel.gameState.value.hexes[stallCoord]?.stall?.upgrades?.get("Damage") == 9 && attempts < 100) {
            viewModel.upgradeStall()
            attempts++
        }

        val upgradedStall = viewModel.gameState.value.hexes[stallCoord]?.stall!!
        assertEquals(10, upgradedStall.upgrades["Damage"])
        // (55 + 5) * 1.25 = 60 * 1.25 = 75
        assertEquals(75, upgradedStall.damage)
    }

    @Test
    fun `upgradeStall allows reaching level 11 Rate even if capped at 50ms`() {
        val application = mockk<Application>(relaxed = true)
        val settingsRepository = mockk<SettingsRepository>()
        val gameStateRepository = mockk<GameStateRepository>(relaxed = true)
        every { settingsRepository.settingsFlow } returns kotlinx.coroutines.flow.flowOf(Settings())

        val viewModel = MainViewModel(application, settingsRepository, gameStateRepository)

        val stallCoord = AxialCoordinate(0, 0)
        // Chicken Rice base rate 700ms. Reduction 70ms.
        // At level 10: 50ms.
        // Level 11 should be allowed and cap at 50ms.
        val stall = Stall(
            id = "s1",
            name = "Chicken Rice",
            cost = 100,
            color = Color.Yellow,
            stallType = StallType.CHICKEN_RICE,
            fireRateMs = 50,
            upgrades = mapOf("Rate" to 10),
            upgradeCount = 10
        )

        viewModel._gameState.value = GameState(
            hexes = mapOf(stallCoord to HexTile(stallCoord, TileType.FLOOR, stall)),
            gold = 10000,
            selectedBoardStall = stallCoord
        )

        var attempts = 0
        while (viewModel.gameState.value.hexes[stallCoord]?.stall?.upgrades?.get("Rate") == 10 && attempts < 100) {
            viewModel.upgradeStall()
            attempts++
        }

        val upgradedStall = viewModel.gameState.value.hexes[stallCoord]?.stall!!
        assertEquals(11, upgradedStall.upgrades["Rate"])
        assertEquals(50L, upgradedStall.fireRateMs)
    }
}
