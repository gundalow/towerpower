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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
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
    val spriteSheet = ImageBitmap.imageResource(id = R.drawable.sprite_sheet)

    val hexWidth = 48.dp
    val hexHeight = hexWidth * 2f / sqrt(3f)

    // Snip constants
    val snipOffset = 69
    val towerSnipOffset = 96
    val pillarSnipOffsetX = 69
    val pillarSnipOffsetY = 139 // Adjusted from 256 block
    val goalSnipOffsetX = 114 // Adjusted from 256 block
    val goalSnipOffsetY = 161 // Adjusted from 256 block

    // Slices
    val floorPlainRect = IntRect(0 + snipOffset, 0 + snipOffset, 0 + snipOffset + 117, 0 + snipOffset + 117)
    val floorCheckeredRect = IntRect(256 + snipOffset, 0 + snipOffset, 256 + snipOffset + 117, 0 + snipOffset + 117)
    val floorDirtyRect = IntRect(512 + snipOffset, 0 + snipOffset, 512 + snipOffset + 117, 0 + snipOffset + 117)
    val floorChopeRect = IntRect(1024 + snipOffset, 256 + snipOffset, 1024 + snipOffset + 117, 256 + snipOffset + 117)
    val edgeNorthRect = IntRect(1280 + snipOffset, 0 + snipOffset, 1280 + snipOffset + 117, 0 + snipOffset + 117)
    val edgeCornerRect = IntRect(1792 + snipOffset, 256 + snipOffset, 1792 + snipOffset + 117, 256 + snipOffset + 117)
    val pillarRect = IntRect(1280 + pillarSnipOffsetX, 512 + pillarSnipOffsetY, 1280 + pillarSnipOffsetX + 117, 512 + pillarSnipOffsetY + 234)
    val goalTableRect = IntRect(1536 + goalSnipOffsetX, 1024 + goalSnipOffsetY, 1536 + goalSnipOffsetX + 284, 1024 + goalSnipOffsetY + 190)

    val uiTehTarikRect = IntRect(0 + towerSnipOffset, 1792 + towerSnipOffset, 0 + towerSnipOffset + 64, 1792 + towerSnipOffset + 64)
    val uiSatayRect = IntRect(256 + towerSnipOffset, 1792 + towerSnipOffset, 256 + towerSnipOffset + 64, 1792 + towerSnipOffset + 64)
    val uiChickenRiceRect = IntRect(512 + towerSnipOffset, 1792 + towerSnipOffset, 512 + towerSnipOffset + 64, 1792 + towerSnipOffset + 64)
    val uiIceKachangRect = uiTehTarikRect // Using Teh Tarik for Ice Kachang for now

    val enemySalarymanRect = IntRect(1024 + snipOffset, 1792 + snipOffset, 1024 + snipOffset + 117, 1792 + snipOffset + 117)
    val enemyRiderRect = IntRect(1536 + snipOffset, 1792 + snipOffset, 1536 + snipOffset + 117, 1792 + snipOffset + 117)
    val fxPuddleRect = IntRect(1792 + snipOffset, 1792 + snipOffset, 1792 + snipOffset + 117, 1792 + snipOffset + 117)

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
                        val wPx = hexWidth.toPx()
                        val hPx = hexHeight.toPx()
                        val borderPx = 20.dp.toPx()

                        val adjustedY = offset.y - borderPx - hPx / 2f
                        val r = Math.round(adjustedY / (hPx * 0.75f)).toInt()

                        val rowOffset = if (r % 2 != 0) wPx / 2f else 0f
                        val adjustedX = offset.x - borderPx - wPx / 2f - rowOffset
                        val q_offset = Math.round(adjustedX / wPx).toInt()

                        // Convert Offset (q_offset, r) back to Axial (q, r)
                        val q = q_offset - (r - (r and 1)) / 2
                        onCellClick(AxialCoordinate(q, r))
                    }
                }
        ) {
            val wPx = hexWidth.toPx()
            val hPx = hexHeight.toPx()
            val borderPx = 20.dp.toPx()

            fun toScreen(q: Int, r: Int): Offset {
                // Axial to Offset
                val q_offset = q + (r - (r and 1)) / 2
                val rowOffset = if (r % 2 != 0) wPx / 2f else 0f
                val x = q_offset * wPx + rowOffset + borderPx + wPx / 2f
                val y = r * (hPx * 0.75f) + borderPx + hPx / 2f
                return Offset(x, y)
            }

            fun toScreenPrecise(q: Float, r: Float): Offset {
                // Axial to Offset
                val q_offset = q + (r.toInt() - (r.toInt() and 1)) / 2f
                val rowOffset = if (r.toInt() % 2 != 0) wPx / 2f else 0f
                val x = q_offset * wPx + rowOffset + borderPx + wPx / 2f
                val y = r * (hPx * 0.75f) + borderPx + hPx / 2f
                return Offset(x, y)
            }

            fun createHexPath(center: Offset, radius: Float): Path {
                return Path().apply {
                    val angleStep = Math.PI.toFloat() / 3f
                    // Pointy-topped hex: vertices are at 30, 90, 150, 210, 270, 330 degrees
                    // Or -30, 30, 90, 150, 210, 270
                    for (i in 0..5) {
                        val angle = angleStep * i - Math.PI.toFloat() / 2f
                        val x = center.x + radius * Math.cos(angle.toDouble()).toFloat()
                        val y = center.y + radius * Math.sin(angle.toDouble()).toFloat()
                        if (i == 0) moveTo(x, y) else lineTo(x, y)
                    }
                    close()
                }
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
                    q = coord.q.toFloat(),
                    r = coord.r.toFloat(),
                    zOrder = 0,
                    draw = {
                        val destSize = when (tile.type) {
                            TileType.PILLAR -> {
                                IntSize((117 * (wPx / 117f)).toInt(), (234 * (wPx / 117f)).toInt())
                            }
                            TileType.GOAL_TABLE -> {
                                IntSize((284 * (wPx / 117f)).toInt(), (190 * (wPx / 117f)).toInt())
                            }
                            else -> {
                                IntSize(wPx.toInt(), hPx.toInt())
                            }
                        }

                        val destOffset = when (tile.type) {
                            TileType.PILLAR -> {
                                // Anchor bottom-center of 117x234 to hex center
                                IntOffset((screenPos.x - destSize.width / 2).toInt(), (screenPos.y - destSize.height).toInt())
                            }
                            TileType.GOAL_TABLE -> {
                                // Anchor center of 284x190 to hex center
                                IntOffset((screenPos.x - destSize.width / 2).toInt(), (screenPos.y - destSize.height / 2).toInt())
                            }
                            else -> {
                                // Anchor center of 117x117 to hex center
                                IntOffset((screenPos.x - destSize.width / 2).toInt(), (screenPos.y - destSize.height / 2).toInt())
                            }
                        }

                        val shouldClip = tile.type != TileType.PILLAR && tile.type != TileType.GOAL_TABLE
                        if (shouldClip) {
                            clipPath(createHexPath(screenPos, hPx / 2f)) {
                                drawImage(
                                    image = spriteSheet,
                                    srcOffset = srcRect.topLeft,
                                    srcSize = srcRect.size,
                                    dstOffset = destOffset,
                                    dstSize = destSize
                                )
                            }
                        } else {
                            drawImage(
                                image = spriteSheet,
                                srcOffset = srcRect.topLeft,
                                srcSize = srcRect.size,
                                dstOffset = destOffset,
                                dstSize = destSize
                            )
                        }
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
                        q = coord.q.toFloat(),
                        r = coord.r.toFloat(),
                        zOrder = 1,
                        draw = {
                            val tW = (64 * (wPx / 117f)).toInt()
                            val tH = (64 * (wPx / 117f)).toInt()
                            drawImage(
                                image = spriteSheet,
                                srcOffset = towerSrcRect.topLeft,
                                srcSize = towerSrcRect.size,
                                dstOffset = IntOffset((screenPos.x - tW / 2).toInt(), (screenPos.y - tH / 2).toInt()),
                                dstSize = IntSize(tW, tH)
                            )
                        }
                    ))
                }
            }

            puddles.forEach { puddle ->
                val screenPos = toScreenPrecise(puddle.position.q, puddle.position.r)
                drawables.add(DrawableEntity(
                    q = puddle.position.q,
                    r = puddle.position.r,
                    zOrder = 0, // Draw on floor layer
                    draw = {
                        clipPath(createHexPath(screenPos, hPx / 2f)) {
                            drawImage(
                                image = spriteSheet,
                                srcOffset = fxPuddleRect.topLeft,
                                srcSize = fxPuddleRect.size,
                                dstOffset = IntOffset((screenPos.x - wPx / 2).toInt(), (screenPos.y - hPx / 2).toInt()),
                                dstSize = IntSize(wPx.toInt(), hPx.toInt())
                            )
                        }
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
                    q = enemy.position.q,
                    r = enemy.position.r,
                    zOrder = 2,
                    draw = {
                        drawImage(
                            image = spriteSheet,
                            srcOffset = enemySrcRect.topLeft,
                            srcSize = enemySrcRect.size,
                            dstOffset = IntOffset((screenPos.x - wPx / 2).toInt(), (screenPos.y - hPx / 2).toInt()),
                            dstSize = IntSize(wPx.toInt(), hPx.toInt())
                        )

                        // Health bar
                        val barWidth = wPx * 0.8f
                        val barHeight = 4.dp.toPx()
                        val healthPercent = enemy.health.toFloat() / enemy.maxHealth

                        drawRect(
                            color = Color.Black,
                            topLeft = Offset(screenPos.x - barWidth / 2, screenPos.y - hPx / 2 - 10f),
                            size = Size(barWidth, barHeight)
                        )
                        drawRect(
                            color = Color.Red,
                            topLeft = Offset(screenPos.x - barWidth / 2, screenPos.y - hPx / 2 - 10f),
                            size = Size(barWidth * healthPercent, barHeight)
                        )
                    }
                ))
            }

            projectiles.forEach { projectile ->
                val screenPos = toScreenPrecise(projectile.position.q, projectile.position.r)
                drawables.add(DrawableEntity(
                    q = projectile.position.q,
                    r = projectile.position.r,
                    zOrder = 3,
                    draw = {
                        drawCircle(
                            color = projectile.color,
                            radius = 4.dp.toPx(),
                            center = screenPos
                        )
                    }
                ))
            }

            // Sort and draw
            val sortedDrawables = drawables.sortedWith(object : Comparator<DrawableEntity> {
                override fun compare(a: DrawableEntity, b: DrawableEntity): Int {
                    val rComp = a.r.compareTo(b.r)
                    if (rComp != 0) return rComp
                    val qComp = a.q.compareTo(b.q)
                    if (qComp != 0) return qComp
                    return a.zOrder.compareTo(b.zOrder)
                }
            })
            sortedDrawables.forEach { it.draw(this) }
        }
    }
}

data class DrawableEntity(
    val q: Float,
    val r: Float,
    val zOrder: Int,
    val draw: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit
)
