package com.example.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.app.ui.components.GameBoard
import com.example.app.ui.components.GameControlPanel
import com.example.app.ui.constants.LayoutConstants
import com.example.app.ui.theme.ExampleAppTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExampleAppTheme {
                val gameState by viewModel.gameState.collectAsState()
                val availableTowers by viewModel.availableTowers.collectAsState()

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
                            onCellClick = { x, y -> viewModel.onCellClick(x, y) },
                            modifier = Modifier.weight(LayoutConstants.BOARD_HEIGHT_FRACTION)
                        )
                        GameControlPanel(
                            gold = gameState.gold,
                            health = gameState.health,
                            availableTowers = availableTowers,
                            selectedTower = gameState.selectedTowerType,
                            onTowerSelected = { tower -> viewModel.selectTower(tower) },
                            modifier = Modifier.weight(LayoutConstants.CONTROL_PANEL_HEIGHT_FRACTION)
                        )
                    }
                }
            }
        }
    }
}
