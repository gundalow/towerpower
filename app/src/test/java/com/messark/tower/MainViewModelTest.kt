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
        val application = mockk<Application>()
        val settingsRepository = mockk<SettingsRepository>()
        every { settingsRepository.settingsFlow } returns kotlinx.coroutines.flow.flowOf(com.messark.tower.model.Settings())

        val viewModel = MainViewModel(application, settingsRepository)
        val state = viewModel.gameState.first()

        assertNotNull(state.grid)
        assertEquals(100, state.health)
        assertEquals(500, state.gold)
        assertNotNull(state.startPosition)
        assertNotNull(state.endPosition)
    }
}
