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
    val hexHeight = hexWidth * 110f / 117f

    // Slices
    val floorPlainRect = IntRect(70, 94, 70 + 117, 94 + 110)
    val floorCheckeredRect = IntRect(326, 94, 326 + 117, 94 + 110)
    val floorDirtyRect = IntRect(1350, 94, 1350 + 117, 94 + 110)
    val floorChopeRect = IntRect(1350, 350, 1350 + 117, 350 + 110)
    val edgeNorthRect = IntRect(1862, 94, 1862 + 117, 94 + 110)
    val edgeCornerRect = IntRect(1862, 606, 1862 + 117, 606 + 110)
    val pillarRect = IntRect(70, 862, 70 + 117, 862 + 234)
    val goalTableRect = IntRect(1606, 862, 1606 + 280, 862 + 200)

    val uiTehTarikRect = IntRect(96, 1888, 96 + 65, 1888 + 65)
    val uiSatayRect = IntRect(352, 1888, 352 + 65, 1888 + 65)
    val uiChickenRiceRect = IntRect(608, 1888, 608 + 65, 1888 + 65)
    val uiDurianRect = IntRect(864, 1888, 864 + 65, 1888 + 65)
    val uiIceKachangRect = IntRect(1120, 1888, 1120 + 65, 1888 + 65)

    val enemySalarymanRect = IntRect(1088, 1632, 1088 + 100, 1632 + 180)
    val enemyTouristRect = IntRect(1344, 1632, 1344 + 110, 1632 + 180)
    val enemyAuntieRect = IntRect(1600, 1632, 1600 + 110, 1632 + 180)
    val enemyRiderRect = IntRect(1580, 1880, 1580 + 160, 1880 + 150)
    val fxPuddleRect = IntRect(1888, 1884, 1888 + 65, 1884 + 65)

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

            fun createHexPath(center: Offset, width: Float, height: Float): Path {
                return Path().apply {
                    moveTo(center.x, center.y - height / 2f)
                    lineTo(center.x + width / 2f, center.y - height / 4f)
                    lineTo(center.x + width / 2f, center.y + height / 4f)
                    lineTo(center.x, center.y + height / 2f)
                    lineTo(center.x - width / 2f, center.y + height / 4f)
                    lineTo(center.x - width / 2f, center.y - height / 4f)
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
                        val scale = wPx / 117f
                        val destSize = when (tile.type) {
                            TileType.PILLAR -> {
                                IntSize((117 * scale).toInt(), (234 * scale).toInt())
                            }
                            TileType.GOAL_TABLE -> {
                                IntSize((280 * scale).toInt(), (200 * scale).toInt())
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
                                // Anchor bottom-center of 280x200 to hex center
                                IntOffset((screenPos.x - destSize.width / 2).toInt(), (screenPos.y - destSize.height).toInt())
                            }
                            else -> {
                                // Anchor center of 117x110 to hex center
                                IntOffset((screenPos.x - destSize.width / 2).toInt(), (screenPos.y - destSize.height / 2).toInt())
                            }
                        }

                        val shouldClip = tile.type != TileType.PILLAR && tile.type != TileType.GOAL_TABLE
                        if (shouldClip) {
                            clipPath(createHexPath(screenPos, wPx, hPx)) {
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
                        StallType.DURIAN -> uiDurianRect
                        StallType.ICE_KACHANG -> uiIceKachangRect
                    }
                    drawables.add(DrawableEntity(
                        q = coord.q.toFloat(),
                        r = coord.r.toFloat(),
                        zOrder = 1,
                        draw = {
                            val scale = wPx / 117f
                            val tW = (65 * scale).toInt()
                            val tH = (65 * scale).toInt()
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
                        val scale = wPx / 117f
                        val pW = (65 * scale).toInt()
                        val pH = (65 * scale).toInt()
                        clipPath(createHexPath(screenPos, wPx, hPx)) {
                            drawImage(
                                image = spriteSheet,
                                srcOffset = fxPuddleRect.topLeft,
                                srcSize = fxPuddleRect.size,
                                dstOffset = IntOffset((screenPos.x - pW / 2).toInt(), (screenPos.y - pH / 2).toInt()),
                                dstSize = IntSize(pW, pH)
                            )
                        }
                    }
                ))
            }

            enemies.forEach { enemy ->
                val screenPos = toScreenPrecise(enemy.position.q, enemy.position.r)
                val enemySrcRect = when (enemy.type) {
                    EnemyType.SALARYMAN -> enemySalarymanRect
                    EnemyType.TOURIST -> enemyTouristRect
                    EnemyType.AUNTIE -> enemyAuntieRect
                    EnemyType.DELIVERY_RIDER -> enemyRiderRect
                }
                drawables.add(DrawableEntity(
                    q = enemy.position.q,
                    r = enemy.position.r,
                    zOrder = 2,
                    draw = {
                        val scale = wPx / 117f
                        val eW = (enemySrcRect.width * scale).toInt()
                        val eH = (enemySrcRect.height * scale).toInt()
                        drawImage(
                            image = spriteSheet,
                            srcOffset = enemySrcRect.topLeft,
                            srcSize = enemySrcRect.size,
                            dstOffset = IntOffset((screenPos.x - eW / 2).toInt(), (screenPos.y - eH).toInt()),
                            dstSize = IntSize(eW, eH)
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
