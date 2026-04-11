package com.messark.tower.utils

import com.messark.tower.model.AxialCoordinate
import com.messark.tower.model.HexTile
import com.messark.tower.model.TileType

object MapGenerator {
    fun generateMap(mapData: List<String>): Map<AxialCoordinate, HexTile> {
        val hexes = mutableMapOf<AxialCoordinate, HexTile>()

        mapData.forEachIndexed { r, row ->
            row.forEachIndexed { q_offset, char ->
                // Convert offset coordinates (from List<String>) to Axial
                // Assuming the input strings represent an odd-r offset grid
                val q = q_offset - (r - (r and 1)) / 2
                val coord = AxialCoordinate(q, r)

                val type = when (char) {
                    'F' -> TileType.FLOOR_PLAIN
                    'C' -> TileType.FLOOR_CHECKERED
                    'D' -> TileType.FLOOR_DIRTY
                    'H' -> TileType.FLOOR_CHOPE
                    'P' -> TileType.PILLAR
                    'G' -> TileType.GOAL_TABLE
                    'N' -> TileType.EDGE_NORTH
                    'E' -> TileType.EDGE_CORNER
                    else -> TileType.FLOOR_PLAIN
                }

                if (char == 'G') {
                    // Goal Table occupies a 7-hex cluster
                    hexes[coord] = HexTile(coord, TileType.GOAL_TABLE)
                    getNeighbors(coord).forEach { neighbor ->
                        hexes[neighbor] = HexTile(neighbor, TileType.GOAL_TABLE)
                    }
                } else if (char != ' ') {
                    // Only place if not already occupied (e.g. by a Goal Table neighbor)
                    if (!hexes.containsKey(coord)) {
                        hexes[coord] = HexTile(coord, type)
                    }
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
