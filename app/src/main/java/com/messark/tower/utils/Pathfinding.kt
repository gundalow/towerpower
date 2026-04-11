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
        val openSetPositions = mutableSetOf<Position>()
        val closedSet = mutableSetOf<Position>()
        val nodes = mutableMapOf<Position, Node>()

        val startNode = Node(start, gScore = 0, hScore = heuristic(start, end))
        openSet.add(startNode)
        openSetPositions.add(start)
        nodes[start] = startNode

        while (openSet.isNotEmpty()) {
            val current = openSet.poll() ?: break
            openSetPositions.remove(current.position)

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

                    if (neighborPos in openSetPositions) {
                        // Re-add to update priority in queue
                        openSet.remove(neighborNode)
                    }
                    openSet.add(neighborNode)
                    openSetPositions.add(neighborPos)
                }
            }
        }

        return null // No path found
    }

    private fun heuristic(a: Position, b: Position): Int {
        // Hex distance for pointy-top odd-r offset coordinates
        val aQ = a.x - (a.y - (a.y and 1)) / 2
        val aR = a.y
        val bQ = b.x - (b.y - (b.y and 1)) / 2
        val bR = b.y

        return (Math.abs(aQ - bQ) + Math.abs(aQ + aR - bQ - bR) + Math.abs(aR - bR)) / 2
    }

    private fun getNeighbors(pos: Position, width: Int, height: Int): List<Position> {
        val neighbors = mutableListOf<Position>()
        val x = pos.x
        val y = pos.y

        // Neighbors same row
        addIfValid(x - 1, y, width, height, neighbors)
        addIfValid(x + 1, y, width, height, neighbors)

        // Neighbors other rows (pointy-top odd-r)
        if (y % 2 == 0) {
            addIfValid(x - 1, y - 1, width, height, neighbors)
            addIfValid(x, y - 1, width, height, neighbors)
            addIfValid(x - 1, y + 1, width, height, neighbors)
            addIfValid(x, y + 1, width, height, neighbors)
        } else {
            addIfValid(x, y - 1, width, height, neighbors)
            addIfValid(x + 1, y - 1, width, height, neighbors)
            addIfValid(x, y + 1, width, height, neighbors)
            addIfValid(x + 1, y + 1, width, height, neighbors)
        }

        return neighbors
    }

    private fun addIfValid(x: Int, y: Int, width: Int, height: Int, list: MutableList<Position>) {
        if (x in 0 until width && y in 0 until height) {
            list.add(Position(x, y))
        }
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
