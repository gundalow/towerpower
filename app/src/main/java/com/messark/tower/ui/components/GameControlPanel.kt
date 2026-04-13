package com.messark.tower.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.messark.tower.R
import com.messark.tower.model.Tower
import com.messark.tower.ui.constants.SpriteConstants

@Composable
fun GameControlPanel(
    gold: Int,
    health: Int,
    availableTowers: List<Tower>,
    selectedTower: Tower?,
    onTowerSelected: (Tower) -> Unit,
    onStartWave: () -> Unit,
    waveActive: Boolean,
    modifier: Modifier = Modifier
) {
    val spriteSheet = ImageBitmap.imageResource(id = R.drawable.sprite_sheet)
    val context = LocalContext.current
    val versionName = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: Exception) {
        "1.0"
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.DarkGray)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(text = "🪑 Tables: $health", color = Color.Red, fontSize = 16.sp)
            Text(text = "💰 Budget: $gold", color = Color.Yellow, fontSize = 16.sp)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 2.dp)
        ) {
            Text(text = "STALL SHOP", color = Color.White, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "v$versionName", color = Color.Gray, fontSize = 10.sp)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(5),
            modifier = Modifier.weight(1f).fillMaxWidth(0.95f),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            contentPadding = PaddingValues(2.dp)
        ) {
            items(availableTowers) { tower ->
                TowerSlot(
                    tower = tower,
                    isSelected = selectedTower?.id == tower.id,
                    onClick = { onTowerSelected(tower) },
                    spriteSheet = spriteSheet
                )
            }
        }

        Button(
            onClick = onStartWave,
            modifier = Modifier.fillMaxWidth().height(40.dp),
            enabled = !waveActive,
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = if (waveActive) "LUNCH RUSH..." else "START LUNCH RUSH",
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun TowerSlot(
    tower: Tower,
    isSelected: Boolean,
    onClick: () -> Unit,
    spriteSheet: ImageBitmap
) {
    val spriteRect = SpriteConstants.STALL_RECTS[tower.stallType] ?: SpriteConstants.STALL_RECTS.values.first()

    Box(
        modifier = Modifier
            .aspectRatio(0.8f) // Slightly taller to accommodate text below
            .background(if (isSelected) Color.White.copy(alpha = 0.3f) else Color.Black.copy(alpha = 0.2f))
            .border(1.dp, if (isSelected) Color.White else Color.Gray)
            .clickable { onClick() }
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Canvas(modifier = Modifier.size(32.dp)) {
                drawImage(
                    image = spriteSheet,
                    srcOffset = spriteRect.topLeft,
                    srcSize = spriteRect.size,
                    dstOffset = IntOffset.Zero,
                    dstSize = IntSize(size.width.toInt(), size.height.toInt())
                )
            }
            Text(
                text = tower.name,
                color = Color.White,
                fontSize = 8.sp,
                maxLines = 1
            )
            Text(
                text = "$${tower.cost}",
                color = Color.Yellow,
                fontSize = 9.sp,
                maxLines = 1
            )
        }
    }
}
