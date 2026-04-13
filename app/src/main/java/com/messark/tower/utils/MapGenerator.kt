package com.messark.tower.utils

import com.messark.tower.model.AxialCoordinate
import com.messark.tower.model.HexTile
import com.messark.tower.model.TileType
import java.util.Random

object MapGenerator {
    private val random = Random()

    fun generateMap(mapData: List<String>): Map<AxialCoordinate, HexTile> {
        val hexes = mutableMapOf<AxialCoordinate, HexTile>()

        mapData.forEachIndexed { r, row ->
            row.forEachIndexed { q_offset, char ->
                // Convert offset coordinates (from List<String>) to Axial
                // Assuming the input strings represent an odd-r offset grid
                val q = q_offset - (r - (r and 1)) / 2
                val coord = AxialCoordinate(q, r)

                val type = when (char) {
                    'F' -> TileType.FLOOR
                    'P' -> TileType.PILLAR
                    'G' -> TileType.GOAL_TABLE
                    '1' -> TileType.EDGE_NW
                    '2' -> TileType.EDGE_NE
                    '3' -> TileType.EDGE_SW
                    '4' -> TileType.EDGE_SE
                    'T' -> TileType.EDGE_TOP
                    ' ' -> TileType.FLOOR
                    else -> TileType.FLOOR
                }

                // Place the tile based on character type
                if (!hexes.containsKey(coord)) {
                    val floorVariant = if (type == TileType.FLOOR) {
                        random.nextInt(8) // 0-7 variants
                    } else 0
                    hexes[coord] = HexTile(coord, type, floorVariant = floorVariant)
                }
            }
        }

        return hexes
    }

    private fun getNeighbors(coord: AxialCoordinate): List<AxialCoordinate> {
        return listOf(
            AxialCoordinate(coord.q + 1, coord.r),
            AxialCoordinate(coord.q + 1, coord.r - 1),
            AxialCoordinate(coord.q, coord.r - 1),
            AxialCoordinate(coord.q - 1, coord.r),
            AxialCoordinate(coord.q - 1, coord.r + 1),
            AxialCoordinate(coord.q, coord.r + 1)
        )
    }
}
