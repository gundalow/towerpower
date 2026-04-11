package com.messark.tower.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.messark.tower.model.Tower

@Composable
fun GameControlPanel(
    gold: Int,
    health: Int,
    availableTowers: List<Tower>,
    selectedTower: Tower?,
    onTowerSelected: (Tower) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = "❤️ $health", color = Color.Red, fontSize = 20.sp)
            Text(text = "💰 $gold", color = Color.Yellow, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(availableTowers) { tower ->
                TowerSlot(
                    tower = tower,
                    isSelected = selectedTower?.id == tower.id,
                    onClick = { onTowerSelected(tower) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { /* Static for now */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "START WAVE")
        }
    }
}

@Composable
fun TowerSlot(
    tower: Tower,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(if (isSelected) Color.White.copy(alpha = 0.3f) else Color.Transparent)
            .border(2.dp, if (isSelected) Color.White else Color.Gray)
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(tower.color)
            )
            Text(text = tower.name, color = Color.White, fontSize = 10.sp)
            Text(text = "${tower.cost}", color = Color.Yellow, fontSize = 10.sp)
        }
    }
}
