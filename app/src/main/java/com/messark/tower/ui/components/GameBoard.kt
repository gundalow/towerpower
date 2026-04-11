package com.messark.tower.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.messark.tower.model.CellType
import com.messark.tower.model.Enemy
import com.messark.tower.model.GridCell
import com.messark.tower.model.Projectile

@Composable
fun GameBoard(
    grid: List<List<GridCell>>,
    enemies: List<Enemy>,
    projectiles: List<Projectile>,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cellSize = 40.dp
    val rows = grid.size
    val cols = if (rows > 0) grid[0].size else 0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20)) // Dark green background
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
    ) {
        Canvas(
            modifier = Modifier
                .size(width = cellSize * cols, height = cellSize * rows)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val x = (offset.x / cellSize.toPx()).toInt()
                        val y = (offset.y / cellSize.toPx()).toInt()
                        if (x in 0 until cols && y in 0 until rows) {
                            onCellClick(x, y)
                        }
                    }
                }
        ) {
            val cellSizePx = cellSize.toPx()

            for (y in 0 until rows) {
                for (x in 0 until cols) {
                    val cell = grid[y][x]
                    val left = x * cellSizePx
                    val top = y * cellSizePx

                    // Draw cell background
                    val color = when (cell.type) {
                        CellType.EMPTY -> Color(0xFF2E7D32)
                        CellType.PATH -> Color(0xFF795548)
                        CellType.START -> Color(0xFFF44336)
                        CellType.END -> Color(0xFF2196F3)
                        CellType.BOULDER -> Color(0xFF5D4037) // Dark brown for boulders
                    }
                    drawRect(
                        color = color,
                        topLeft = Offset(left, top),
                        size = Size(cellSizePx, cellSizePx)
                    )

                    // Draw grid lines
                    drawRect(
                        color = Color.Black.copy(alpha = 0.2f),
                        topLeft = Offset(left, top),
                        size = Size(cellSizePx, cellSizePx),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                    )

                    // Draw tower if present
                    cell.tower?.let { tower ->
                        drawCircle(
                            color = tower.color,
                            radius = cellSizePx * 0.4f,
                            center = Offset(left + cellSizePx / 2, top + cellSizePx / 2)
                        )
                    }
                }
            }

            // Draw enemies
            enemies.forEach { enemy ->
                val centerX = enemy.position.x * cellSizePx + cellSizePx / 2
                val centerY = enemy.position.y * cellSizePx + cellSizePx / 2

                // Enemy body
                drawCircle(
                    color = Color.Red,
                    radius = cellSizePx * 0.3f,
                    center = Offset(centerX, centerY)
                )

                // Health bar
                val barWidth = cellSizePx * 0.6f
                val barHeight = 4.dp.toPx()
                val healthPercent = enemy.health.toFloat() / enemy.maxHealth

                drawRect(
                    color = Color.Black,
                    topLeft = Offset(centerX - barWidth / 2, centerY - cellSizePx * 0.5f),
                    size = Size(barWidth, barHeight)
                )
                drawRect(
                    color = Color.Green,
                    topLeft = Offset(centerX - barWidth / 2, centerY - cellSizePx * 0.5f),
                    size = Size(barWidth * healthPercent, barHeight)
                )
            }

            // Draw projectiles
            projectiles.forEach { projectile ->
                val centerX = projectile.position.x * cellSizePx + cellSizePx / 2
                val centerY = projectile.position.y * cellSizePx + cellSizePx / 2

                drawCircle(
                    color = projectile.color,
                    radius = 4.dp.toPx(),
                    center = Offset(centerX, centerY)
                )
            }
        }
    }
}
