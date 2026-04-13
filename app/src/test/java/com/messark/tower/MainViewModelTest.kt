package com.messark.tower

import android.app.Application
import com.messark.tower.utils.SettingsRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `gameState is initialized correctly`() = runBlocking {
        val application = mockk<Application>(relaxed = true)
        val settingsRepository = mockk<SettingsRepository>()
        every { settingsRepository.settingsFlow } returns kotlinx.coroutines.flow.flowOf(com.messark.tower.model.Settings())

        val viewModel = MainViewModel(application, settingsRepository)
        val state = viewModel.gameState.first()

        assertTrue(state.hexes.isNotEmpty())
        assertEquals(10, state.health) // Hawker Center Hustle uses 10 tables
        assertEquals(500, state.gold)
        assertNotNull(state.startPosition)
        assertNotNull(state.endPosition)
    }

    @Test
    fun `availableTowers have correct descriptions`() = runBlocking {
        val application = mockk<Application>(relaxed = true)
        val settingsRepository = mockk<SettingsRepository>()
        every { settingsRepository.settingsFlow } returns kotlinx.coroutines.flow.flowOf(com.messark.tower.model.Settings())

        val viewModel = MainViewModel(application, settingsRepository)
        val towers = viewModel.availableTowers.value

        assertEquals(5, towers.size)
        assertEquals("Creates slowing puddles", towers.find { it.name == "Teh Tarik" }?.description)
        assertEquals("Fast area damage", towers.find { it.name == "Satay" }?.description)
        assertEquals("High single-target damage", towers.find { it.name == "Chicken Rice" }?.description)
        assertEquals("Massive damage, slow fire", towers.find { it.name == "Durian" }?.description)
        assertEquals("Freezes enemies in place", towers.find { it.name == "Ice Kachang" }?.description)
    }
}
