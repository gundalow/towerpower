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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.messark.tower.R
import com.messark.tower.model.*
import com.messark.tower.ui.constants.SpriteConstants
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
    val hexHeight = hexWidth * 91f / 101f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1B5E20))
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
    ) {
        Canvas(
            modifier = Modifier
                .size(2000.dp, 2000.dp)
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

                        val q = q_offset - (r - (r and 1)) / 2
                        onCellClick(AxialCoordinate(q, r))
                    }
                }
        ) {
            val wPx = hexWidth.toPx()
            val hPx = hexHeight.toPx()
            val borderPx = 20.dp.toPx()

            fun toScreen(q: Int, r: Int): Offset {
                val q_offset = q + (r - (r and 1)) / 2
                val rowOffset = if (r % 2 != 0) wPx / 2f else 0f
                val x = q_offset * wPx + rowOffset + borderPx + wPx / 2f
                val y = r * (hPx * 0.75f) + borderPx + hPx / 2f
                return Offset(x, y)
            }

            fun toScreenPrecise(q: Float, r: Float): Offset {
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

            val drawables = mutableListOf<DrawableEntity>()

            hexes.forEach { (coord, tile) ->
                val screenPos = toScreen(coord.q, coord.r)

                // Always draw floor under anything that isn't an edge tile
                if (!tile.type.name.startsWith("EDGE_")) {
                    drawables.add(DrawableEntity(
                        q = coord.q.toFloat(),
                        r = coord.r.toFloat(),
                        zOrder = 0,
                        draw = {
                            val floorSrc = SpriteConstants.FLOOR_RECTS[tile.floorVariant % SpriteConstants.FLOOR_RECTS.size]
                            val destSize = IntSize(wPx.toInt(), hPx.toInt())
                            val destOffset = IntOffset((screenPos.x - destSize.width / 2).toInt(), (screenPos.y - destSize.height / 2).toInt())
                            clipPath(createHexPath(screenPos, wPx, hPx)) {
                                drawImage(
                                    image = spriteSheet,
                                    srcOffset = floorSrc.topLeft,
                                    srcSize = floorSrc.size,
                                    dstOffset = destOffset,
                                    dstSize = destSize,
                                    blendMode = BlendMode.SrcOver,
                                    filterQuality = FilterQuality.Low
                                )
                            }
                        }
                    ))
                }

                if (tile.type != TileType.FLOOR) {
                    val srcRect = when (tile.type) {
                        TileType.PILLAR -> SpriteConstants.PILLAR_RECT
                        TileType.GOAL_TABLE -> SpriteConstants.GOAL_TABLE_RECT
                        TileType.EDGE_NW -> SpriteConstants.EDGE_NW_RECT
                        TileType.EDGE_NE -> SpriteConstants.EDGE_NE_RECT
                        TileType.EDGE_SW -> SpriteConstants.EDGE_SW_RECT
                        TileType.EDGE_SE -> SpriteConstants.EDGE_SE_RECT
                        TileType.EDGE_TOP -> SpriteConstants.EDGE_TOP_RECT
                        else -> null
                    }

                    if (srcRect != null) {
                        drawables.add(DrawableEntity(
                            q = coord.q.toFloat(),
                            r = coord.r.toFloat(),
                            zOrder = if (tile.type.name.startsWith("EDGE_")) 0 else 1,
                            draw = {
                                val scale = wPx / 101f
                                val destSize = when (tile.type) {
                                    TileType.PILLAR -> IntSize((SpriteConstants.PILLAR_RECT.width * scale).toInt(), (SpriteConstants.PILLAR_RECT.height * scale).toInt())
                                    TileType.GOAL_TABLE -> IntSize((SpriteConstants.GOAL_TABLE_RECT.width * scale).toInt(), (SpriteConstants.GOAL_TABLE_RECT.height * scale).toInt())
                                    else -> IntSize(wPx.toInt(), hPx.toInt())
                                }

                                val destOffset = when (tile.type) {
                                    TileType.PILLAR -> IntOffset((screenPos.x - destSize.width / 2).toInt(), (screenPos.y - destSize.height * 0.75f).toInt())
                                    TileType.GOAL_TABLE -> IntOffset((screenPos.x - destSize.width / 2).toInt(), (screenPos.y - destSize.height).toInt())
                                    else -> IntOffset((screenPos.x - destSize.width / 2).toInt(), (screenPos.y - destSize.height / 2).toInt())
                                }

                                if (tile.type.name.startsWith("EDGE_")) {
                                    clipPath(createHexPath(screenPos, wPx, hPx)) {
                                        drawImage(
                                            image = spriteSheet,
                                            srcOffset = srcRect.topLeft,
                                            srcSize = srcRect.size,
                                            dstOffset = destOffset,
                                            dstSize = destSize,
                                            blendMode = BlendMode.SrcOver,
                                            filterQuality = FilterQuality.Low
                                        )
                                    }
                                } else {
                                    drawImage(
                                        image = spriteSheet,
                                        srcOffset = srcRect.topLeft,
                                        srcSize = srcRect.size,
                                        dstOffset = destOffset,
                                        dstSize = destSize,
                                        blendMode = BlendMode.SrcOver,
                                        filterQuality = FilterQuality.Low
                                    )
                                }
                            }
                        ))
                    }
                }

                tile.tower?.let { tower ->
                    val towerSrcRect = SpriteConstants.STALL_RECTS[tower.stallType] ?: SpriteConstants.STALL_RECTS[StallType.CHICKEN_RICE]!!
                    drawables.add(DrawableEntity(
                        q = coord.q.toFloat(),
                        r = coord.r.toFloat(),
                        zOrder = 1,
                        draw = {
                            val scale = wPx / 101f
                            val tW = (65 * scale).toInt()
                            val tH = (65 * scale).toInt()
                            drawImage(
                                image = spriteSheet,
                                srcOffset = towerSrcRect.topLeft,
                                srcSize = towerSrcRect.size,
                                dstOffset = IntOffset((screenPos.x - tW / 2).toInt(), (screenPos.y - tH / 2).toInt()),
                                dstSize = IntSize(tW, tH),
                                blendMode = BlendMode.SrcOver,
                                filterQuality = FilterQuality.Low
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
                    zOrder = 0,
                    draw = {
                        val scale = wPx / 101f
                        val pW = (64 * scale).toInt()
                        val pH = (62 * scale).toInt()
                        clipPath(createHexPath(screenPos, wPx, hPx)) {
                            drawImage(
                                image = spriteSheet,
                                srcOffset = SpriteConstants.FX_PUDDLE_RECT.topLeft,
                                srcSize = SpriteConstants.FX_PUDDLE_RECT.size,
                                dstOffset = IntOffset((screenPos.x - pW / 2).toInt(), (screenPos.y - pH / 2).toInt()),
                                dstSize = IntSize(pW, pH),
                                blendMode = BlendMode.SrcOver,
                                filterQuality = FilterQuality.Low
                            )
                        }
                    }
                ))
            }

            enemies.forEach { enemy ->
                val screenPos = toScreenPrecise(enemy.position.q, enemy.position.r)
                val enemySrcRect = when (enemy.type) {
                    EnemyType.SALARYMAN -> SpriteConstants.ENEMY_SALARYMAN_RECT
                    EnemyType.TOURIST -> SpriteConstants.ENEMY_TOURIST_RECT
                    EnemyType.AUNTIE -> SpriteConstants.ENEMY_AUNTIE_RECT
                    EnemyType.DELIVERY_RIDER -> SpriteConstants.ENEMY_RIDER_RECT
                }
                drawables.add(DrawableEntity(
                    q = enemy.position.q,
                    r = enemy.position.r,
                    zOrder = 2,
                    draw = {
                        val scale = wPx / 101f
                        val eW = (enemySrcRect.width * scale).toInt()
                        val eH = (enemySrcRect.height * scale).toInt()
                        drawImage(
                            image = spriteSheet,
                            srcOffset = enemySrcRect.topLeft,
                            srcSize = enemySrcRect.size,
                            dstOffset = IntOffset((screenPos.x - eW / 2).toInt(), (screenPos.y - eH).toInt()),
                            dstSize = IntSize(eW, eH),
                            blendMode = BlendMode.SrcOver,
                            filterQuality = FilterQuality.Low
                        )

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

            // Depth sorting: 
            // 1. All ground elements (zOrder == 0) first.
            // 2. All standing elements (zOrder > 0) sorted by their Y-coordinate (row 'r').
            val sortedDrawables = drawables.sortedWith(object : Comparator<DrawableEntity> {
                override fun compare(a: DrawableEntity, b: DrawableEntity): Int {
                    val aGroup = if (a.zOrder == 0) 0 else 1
                    val bGroup = if (b.zOrder == 0) 0 else 1
                    
                    if (aGroup != bGroup) return aGroup.compareTo(bGroup)
                    
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
