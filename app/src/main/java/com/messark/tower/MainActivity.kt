package com.messark.tower

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.messark.tower.ui.components.GameBoard
import com.messark.tower.ui.components.GameControlPanel
import com.messark.tower.ui.constants.LayoutConstants
import com.messark.tower.ui.theme.TowerPowerTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TowerPowerTheme {
                val gameState by viewModel.gameState.collectAsState()
                val availableTowers by viewModel.availableTowers.collectAsState()
                val haptic = LocalHapticFeedback.current

                LaunchedEffect(Unit) {
                    viewModel.hapticEvents.collect {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                            .padding(LayoutConstants.BOARD_BORDER_SIZE)
                    ) {
                        GameBoard(
                            grid = gameState.grid,
                            enemies = gameState.enemies,
                            projectiles = gameState.projectiles,
                            puddles = gameState.puddles,
                            onCellClick = { x, y -> viewModel.onCellClick(x, y) },
                            modifier = Modifier.weight(LayoutConstants.BOARD_HEIGHT_FRACTION)
                        )
                        GameControlPanel(
                            gold = gameState.gold,
                            health = gameState.health,
                            availableTowers = availableTowers,
                            selectedTower = gameState.selectedTowerType,
                            onTowerSelected = { tower -> viewModel.selectTower(tower) },
                            onStartWave = { viewModel.startWave() },
                            waveActive = gameState.waveActive,
                            modifier = Modifier.weight(LayoutConstants.CONTROL_PANEL_HEIGHT_FRACTION)
                        )
                    }
                }
            }
        }
    }
}
