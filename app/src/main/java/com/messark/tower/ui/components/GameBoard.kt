package com.messark.tower.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.messark.tower.R
import com.messark.tower.model.*
import kotlin.math.sqrt
import java.util.Comparator

@Composable
fun GameBoard(
    hexes: Map<AxialCoordinate, HexTile>,
    enemies: List<Enemy>,
    projectiles: List<Projectile>,
    puddles: List<StickyPuddle>,
    onCellClick: (AxialCoordinate) -> Unit,
    modifier: Modifier = Modifier
) {
    val spriteSheet = ImageBitmap.imageResource(id = R.drawable.sprite_sheet) // Assuming 1775912114044 is mapped to sprite_sheet

    val hexSize = 64.dp // Base size for hexes

    // Slices
    val floorPlainRect = IntRect(0, 0, 256, 256)
    val floorCheckeredRect = IntRect(256, 0, 512, 256)
    val floorDirtyRect = IntRect(512, 0, 768, 256)
    val floorChopeRect = IntRect(1024, 256, 1280, 512)
    val edgeNorthRect = IntRect(1280, 0, 1536, 256)
    val edgeCornerRect = IntRect(1792, 256, 2048, 512)
    val pillarRect = IntRect(1280, 512, 1536, 1024)
    val goalTableRect = IntRect(1536, 1024, 2048, 1536)

    val uiTehTarikRect = IntRect(0, 1792, 256, 2048)
    val uiSatayRect = IntRect(256, 1792, 512, 2048)
    val uiChickenRiceRect = IntRect(512, 1792, 768, 2048)
    val uiIceKachangRect = IntRect(0, 1792, 256, 2048) // Using Teh Tarik for Ice Kachang for now

    val enemySalarymanRect = IntRect(1024, 1792, 1280, 2048)
    val enemyRiderRect = IntRect(1536, 1792, 1792, 2048)
    val fxPuddleRect = IntRect(1792, 1792, 2048, 2048)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20))
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
    ) {
        Canvas(
            modifier = Modifier
                .size(2000.dp, 2000.dp) // Large fixed size for scrolling, can be calculated
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val hexSizePx = hexSize.toPx()
                        // Simple inverse axial projection
                        val r = (offset.y / (hexSizePx * 1.5f)).toInt()
                        val q = ((offset.x - (r * hexSizePx * sqrt(3f) / 2f)) / (hexSizePx * sqrt(3f))).toInt()
                        onCellClick(AxialCoordinate(q, r))
                    }
                }
        ) {
            val hexSizePx = hexSize.toPx()
            val W = sqrt(3f) * hexSizePx
            val H = 2f * hexSizePx

            fun toScreen(q: Int, r: Int): Offset {
                val x = W * (q + r / 2f)
                val y = H * (3/4f) * r
                return Offset(x, y)
            }

            fun toScreenPrecise(q: Float, r: Float): Offset {
                val x = W * (q + r / 2f)
                val y = H * (3/4f) * r
                return Offset(x, y)
            }

            // Group all drawables for Z-ordering
            val drawables = mutableListOf<DrawableEntity>()

            hexes.forEach { (coord, tile) ->
                val screenPos = toScreen(coord.q, coord.r)
                val srcRect = when (tile.type) {
                    TileType.FLOOR_PLAIN -> floorPlainRect
                    TileType.FLOOR_CHECKERED -> floorCheckeredRect
                    TileType.FLOOR_DIRTY -> floorDirtyRect
                    TileType.FLOOR_CHOPE -> floorChopeRect
                    TileType.PILLAR -> pillarRect
                    TileType.GOAL_TABLE -> goalTableRect
                    TileType.EDGE_NORTH -> edgeNorthRect
                    TileType.EDGE_CORNER -> edgeCornerRect
                    else -> floorPlainRect
                }

                // Draw floor always at the bottom of its Z-layer
                drawables.add(DrawableEntity(
                    y = screenPos.y,
                    zOrder = 0,
                    draw = {
                        val destSize = if (tile.type == TileType.PILLAR) {
                            IntSize(W.toInt(), (H * 2).toInt())
                        } else if (tile.type == TileType.GOAL_TABLE) {
                            IntSize((W * 2).toInt(), (H * 2).toInt())
                        } else {
                            IntSize(W.toInt(), H.toInt())
                        }

                        val destOffset = if (tile.type == TileType.PILLAR) {
                            IntOffset(screenPos.x.toInt(), (screenPos.y - H).toInt())
                        } else if (tile.type == TileType.GOAL_TABLE) {
                            IntOffset((screenPos.x - W/2).toInt(), (screenPos.y - H).toInt())
                        } else {
                            IntOffset(screenPos.x.toInt(), screenPos.y.toInt())
                        }

                        drawImage(
                            image = spriteSheet,
                            srcOffset = srcRect.topLeft,
                            srcSize = srcRect.size,
                            dstOffset = destOffset,
                            dstSize = destSize
                        )
                    }
                ))

                tile.tower?.let { tower ->
                    val towerSrcRect = when (tower.stallType) {
                        StallType.TEH_TARIK -> uiTehTarikRect
                        StallType.SATAY -> uiSatayRect
                        StallType.CHICKEN_RICE -> uiChickenRiceRect
                        StallType.ICE_KACHANG -> uiIceKachangRect
                    }
                    drawables.add(DrawableEntity(
                        y = screenPos.y,
                        zOrder = 1,
                        draw = {
                            drawImage(
                                image = spriteSheet,
                                srcOffset = towerSrcRect.topLeft,
                                srcSize = towerSrcRect.size,
                                dstOffset = IntOffset(screenPos.x.toInt(), screenPos.y.toInt()),
                                dstSize = IntSize(W.toInt(), H.toInt())
                            )
                        }
                    ))
                }
            }

            puddles.forEach { puddle ->
                val screenPos = toScreenPrecise(puddle.position.q, puddle.position.r)
                drawables.add(DrawableEntity(
                    y = screenPos.y,
                    zOrder = 0, // Draw on floor layer
                    draw = {
                        drawImage(
                            image = spriteSheet,
                            srcOffset = fxPuddleRect.topLeft,
                            srcSize = fxPuddleRect.size,
                            dstOffset = IntOffset(screenPos.x.toInt(), screenPos.y.toInt()),
                            dstSize = IntSize(W.toInt(), H.toInt())
                        )
                    }
                ))
            }

            enemies.forEach { enemy ->
                val screenPos = toScreenPrecise(enemy.position.q, enemy.position.r)
                val enemySrcRect = when (enemy.type) {
                    EnemyType.SALARYMAN -> enemySalarymanRect
                    EnemyType.TOURIST -> enemySalarymanRect // Use same for now
                    EnemyType.DELIVERY_RIDER -> enemyRiderRect
                }
                drawables.add(DrawableEntity(
                    y = screenPos.y,
                    zOrder = 2,
                    draw = {
                        drawImage(
                            image = spriteSheet,
                            srcOffset = enemySrcRect.topLeft,
                            srcSize = enemySrcRect.size,
                            dstOffset = IntOffset(screenPos.x.toInt(), screenPos.y.toInt()),
                            dstSize = IntSize(W.toInt(), H.toInt())
                        )

                        // Health bar
                        val barWidth = W * 0.8f
                        val barHeight = 4.dp.toPx()
                        val healthPercent = enemy.health.toFloat() / enemy.maxHealth

                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(screenPos.x + W * 0.1f, screenPos.y - 10f),
                            size = Size(barWidth, barHeight)
                        )
                        drawRect(
                            color = Color.Red,
                            topLeft = Offset(screenPos.x + W * 0.1f, screenPos.y - 10f),
                            size = Size(barWidth * healthPercent, barHeight)
                        )
                    }
                ))
            }

            projectiles.forEach { projectile ->
                val screenPos = toScreenPrecise(projectile.position.q, projectile.position.r)
                drawables.add(DrawableEntity(
                    y = screenPos.y,
                    zOrder = 3,
                    draw = {
                        drawCircle(
                            color = projectile.color,
                            radius = 4.dp.toPx(),
                            center = Offset(screenPos.x + W/2, screenPos.y + H/2)
                        )
                    }
                ))
            }

            // Sort and draw
            val sortedDrawables = drawables.sortedWith(object : Comparator<DrawableEntity> {
                override fun compare(a: DrawableEntity, b: DrawableEntity): Int {
                    val yComp = a.y.compareTo(b.y)
                    return if (yComp != 0) yComp else a.zOrder.compareTo(b.zOrder)
                }
            })
            sortedDrawables.forEach { it.draw(this) }
        }
    }
}

data class DrawableEntity(
    val y: Float,
    val zOrder: Int,
    val draw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit
)
