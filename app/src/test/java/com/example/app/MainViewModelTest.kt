package com.example.app

import android.app.Application
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MainViewModelTest {
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
