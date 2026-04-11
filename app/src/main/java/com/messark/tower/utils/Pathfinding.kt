package com.messark.tower.utils

import com.messark.tower.model.Position
import java.util.*

object Pathfinding {
    fun findPath(
        start: Position,
        end: Position,
        gridWidth: Int,
        gridHeight: Int,
        blockedPositions: Set<Position>
    ): List<Position>? {
        val openSet = PriorityQueue<Node>(compareBy { it.fScore })
        val closedSet = mutableSetOf<Position>()
        val nodes = mutableMapOf<Position, Node>()

        val startNode = Node(start, gScore = 0, hScore = heuristic(start, end))
        openSet.add(startNode)
        nodes[start] = startNode

        while (openSet.isNotEmpty()) {
            val current = openSet.poll() ?: break

            if (current.position == end) {
                return reconstructPath(current)
            }

            closedSet.add(current.position)

            for (neighborPos in getNeighbors(current.position, gridWidth, gridHeight)) {
                if (neighborPos in blockedPositions || neighborPos in closedSet) continue

                val tentativeGScore = current.gScore + 1
                val neighborNode = nodes.getOrPut(neighborPos) { Node(neighborPos) }

                if (tentativeGScore < neighborNode.gScore) {
                    neighborNode.parent = current
                    neighborNode.gScore = tentativeGScore
                    neighborNode.hScore = heuristic(neighborPos, end)
                    if (neighborPos !in openSet.map { it.position }) {
                        openSet.add(neighborNode)
                    }
                }
            }
        }

        return null // No path found
    }

    private fun heuristic(a: Position, b: Position): Int {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y)
    }

    private fun getNeighbors(pos: Position, width: Int, height: Int): List<Position> {
        val neighbors = mutableListOf<Position>()
        if (pos.x > 0) neighbors.add(Position(pos.x - 1, pos.y))
        if (pos.x < width - 1) neighbors.add(Position(pos.x + 1, pos.y))
        if (pos.y > 0) neighbors.add(Position(pos.x, pos.y - 1))
        if (pos.y < height - 1) neighbors.add(Position(pos.x, pos.y + 1))
        return neighbors
    }

    private fun reconstructPath(node: Node): List<Position> {
        val path = mutableListOf<Position>()
        var current: Node? = node
        while (current != null) {
            path.add(current.position)
            current = current.parent
        }
        return path.reversed()
    }

    private class Node(
        val position: Position,
        var parent: Node? = null,
        var gScore: Int = Int.MAX_VALUE,
        var hScore: Int = 0
    ) {
        val fScore: Int get() = gScore + hScore
    }
}
