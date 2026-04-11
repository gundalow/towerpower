package com.example.app.ui.components

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
import com.example.app.model.CellType
import com.example.app.model.GridCell

@Composable
fun GameBoard(
    grid: List<List<GridCell>>,
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
        }
    }
}
