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

import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import com.messark.tower.model.*

@Composable
fun GameBoard(
    grid: List<List<GridCell>>,
    enemies: List<Enemy>,
    projectiles: List<Projectile>,
    puddles: List<StickyPuddle>,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val hexSize = 30.dp
    val rows = grid.size
    val cols = if (rows > 0) grid[0].size else 0

    val hexWidth = Math.sqrt(3.0).toFloat() * hexSize.value
    val hexHeight = 2f * hexSize.value
    val horizontalSpacing = hexWidth
    val verticalSpacing = hexHeight * 0.75f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20)) // Dark green background
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
    ) {
        Canvas(
            modifier = Modifier
                .size(
                    width = (cols * horizontalSpacing + hexWidth / 2).dp,
                    height = (rows * verticalSpacing + hexHeight / 4).dp
                )
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Reverse hex projection (simplified)
                        val y = (offset.y / (verticalSpacing.dp.toPx())).toInt()
                        val xOffset = if (y % 2 != 0) (horizontalSpacing.dp.toPx() / 2f) else 0f
                        val x = ((offset.x - xOffset) / (horizontalSpacing.dp.toPx())).toInt()

                        if (x in 0 until cols && y in 0 until rows) {
                            onCellClick(x, y)
                        }
                    }
                }
        ) {
            val hexSizePx = hexSize.toPx()
            val hexWidthPx = Math.sqrt(3.0).toFloat() * hexSizePx
            val hexHeightPx = 2f * hexSizePx
            val hSpacingPx = hexWidthPx
            val vSpacingPx = hexHeightPx * 0.75f

            fun getHexCenter(x: Int, y: Int): Offset {
                val xOffset = if (y % 2 != 0) hSpacingPx / 2f else 0f
                return Offset(x * hSpacingPx + xOffset + hexWidthPx / 2, y * vSpacingPx + hexHeightPx / 2)
            }

            fun getPreciseHexCenter(x: Float, y: Float): Offset {
                val yInt = y.toInt()
                val xOffset = if (yInt % 2 != 0) hSpacingPx / 2f else 0f
                // This is a simplification for smooth movement between hexes
                // A better approach would be to interpolate the xOffset based on y
                val nextYInt = (y + 1).toInt()
                val nextXOffset = if (nextYInt % 2 != 0) hSpacingPx / 2f else 0f
                val interpolatedXOffset = xOffset + (nextXOffset - xOffset) * (y - yInt)

                return Offset(x * hSpacingPx + interpolatedXOffset + hexWidthPx / 2, y * vSpacingPx + hexHeightPx / 2)
            }

            // Draw grid
            for (y in 0 until rows) {
                for (x in 0 until cols) {
                    val cell = grid[y][x]
                    val center = getHexCenter(x, y)

                    val color = when (cell.type) {
                        CellType.EMPTY -> Color(0xFF2E7D32)
                        CellType.PATH -> Color(0xFF795548)
                        CellType.START -> Color(0xFFF44336)
                        CellType.END -> Color(0xFF2196F3)
                        CellType.PILLAR -> Color(0xFF5D4037)
                    }

                    val hexPath = Path().apply {
                        for (i in 0 until 6) {
                            val angleDeg = 60f * i - 30f
                            val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
                            val px = center.x + hexSizePx * Math.cos(angleRad.toDouble()).toFloat()
                            val py = center.y + hexSizePx * Math.sin(angleRad.toDouble()).toFloat()
                            if (i == 0) moveTo(px, py) else lineTo(px, py)
                        }
                        close()
                    }

                    drawPath(path = hexPath, color = color)
                    drawPath(
                        path = hexPath,
                        color = Color.Black.copy(alpha = 0.1f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f)
                    )

                    // Draw tower
                    cell.tower?.let { tower ->
                        rotate(degrees = Math.toDegrees(tower.rotation.toDouble()).toFloat(), pivot = center) {
                            if (tower.stallType == StallType.SATAY) {
                                // Draw cone
                                val conePath = Path().apply {
                                    moveTo(center.x, center.y)
                                    val r = tower.range * hSpacingPx
                                    val a1 = -Math.PI / 8
                                    val a2 = Math.PI / 8
                                    lineTo(
                                        center.x + r * Math.cos(a1).toFloat(),
                                        center.y + r * Math.sin(a1).toFloat()
                                    )
                                    lineTo(
                                        center.x + r * Math.cos(a2).toFloat(),
                                        center.y + r * Math.sin(a2).toFloat()
                                    )
                                    close()
                                }
                                drawPath(conePath, color = tower.color.copy(alpha = 0.3f))
                            }
                            drawCircle(
                                color = tower.color,
                                radius = hexSizePx * 0.6f,
                                center = center
                            )
                        }
                    }
                }
            }

            // Draw puddles
            puddles.forEach { puddle ->
                val center = getPreciseHexCenter(puddle.position.x, puddle.position.y)
                drawCircle(
                    color = Color(0xFF795548).copy(alpha = 0.6f), // Brownish sticky puddle
                    radius = hexSizePx * 0.8f,
                    center = center
                )
            }

            // Draw enemies
            enemies.forEach { enemy ->
                val center = getPreciseHexCenter(enemy.position.x, enemy.position.y)

                val enemyColor = when (enemy.type) {
                    EnemyType.SALARYMAN -> Color.White
                    EnemyType.TOURIST -> Color.Yellow
                    EnemyType.DELIVERY_RIDER -> Color.Black
                }

                drawCircle(
                    color = enemyColor,
                    radius = hexSizePx * 0.4f,
                    center = center
                )

                if (enemy.freezeDurationMs > 0) {
                    drawCircle(
                        color = Color.Cyan.copy(alpha = 0.5f),
                        radius = hexSizePx * 0.5f,
                        center = center
                    )
                }

                // Health bar (Hunger meter)
                val barWidth = hexSizePx * 1.0f
                val barHeight = 4.dp.toPx()
                val healthPercent = enemy.health.toFloat() / enemy.maxHealth

                drawRect(
                    color = Color.Black,
                    topLeft = Offset(center.x - barWidth / 2, center.y - hexSizePx * 0.7f),
                    size = Size(barWidth, barHeight)
                )
                drawRect(
                    color = Color.Red, // Hungry meter depletes
                    topLeft = Offset(center.x - barWidth / 2, center.y - hexSizePx * 0.7f),
                    size = Size(barWidth * healthPercent, barHeight)
                )
            }

            // Draw projectiles
            projectiles.forEach { projectile ->
                val center = getPreciseHexCenter(projectile.position.x, projectile.position.y)

                drawCircle(
                    color = projectile.color,
                    radius = 4.dp.toPx(),
                    center = center
                )
            }
        }
    }
}
