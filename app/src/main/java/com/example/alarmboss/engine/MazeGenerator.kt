package com.example.alarmboss.engine

import kotlin.random.Random

object MazeGenerator {
    /**
     * Generates a maze of size width x height (must be odd numbers).
     * 0 = path, 1 = wall, 2 = start, 3 = exit
     */
    fun generate(width: Int, height: Int): Array<IntArray> {
        require(width >= 5 && height >= 5 && width % 2 == 1 && height % 2 == 1) {
            "Maze dimensions must be odd and at least 5, got ${width}x$height"
        }
        val maze = Array(height) { IntArray(width) { 1 } }
        
        // Recursive backtracking algorithm
        fun carve(r: Int, c: Int) {
            val dirs = listOf(
                Pair(-2, 0), Pair(2, 0), Pair(0, -2), Pair(0, 2)
            ).shuffled(Random)
            
            for ((dr, dc) in dirs) {
                val nr = r + dr
                val nc = c + dc
                
                if (nr in 1 until height - 1 && nc in 1 until width - 1 && maze[nr][nc] == 1) {
                    maze[r + dr / 2][c + dc / 2] = 0 // carve wall
                    maze[nr][nc] = 0 // carve cell
                    carve(nr, nc)
                }
            }
        }

        // Start carving from (1, 1)
        maze[1][1] = 0
        carve(1, 1)

        // Set start and exit
        maze[1][1] = 2
        maze[height - 2][width - 2] = 3

        return maze
    }
}
