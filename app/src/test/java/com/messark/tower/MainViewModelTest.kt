package com.messark.tower

import android.app.Application
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

        val viewModel = MainViewModel(application)
        val state = viewModel.gameState.first()

        assertNotNull(state.grid)
        assertEquals(100, state.health)
        assertEquals(500, state.gold)
        assertNotNull(state.startPosition)
        assertNotNull(state.endPosition)
    }
}
