package com.example.alarmboss.ui.ringing.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import com.example.alarmboss.R
import com.example.alarmboss.engine.MazeGenerator

private fun findStart(maze: Array<IntArray>): Pair<Int, Int> {
    for (r in maze.indices) {
        for (c in maze[r].indices) {
            if (maze[r][c] == 2) return r to c
        }
    }
    return 1 to 1
}

@Composable
fun MazeTaskScreen(difficulty: Int = 1, onSolved: () -> Unit) {
    // 0 = path, 1 = wall, 2 = start, 3 = exit. Grows from 5x5 (difficulty 1-2) to 11x11 (9+).
    val size = 5 + 2 * (difficulty / 3).coerceIn(0, 3)
    val maze = remember(size) { MazeGenerator.generate(size, size) }
    val start = remember(maze) { findStart(maze) }

    var playerRow by remember(maze) { mutableIntStateOf(start.first) }
    var playerCol by remember(maze) { mutableIntStateOf(start.second) }

    val moveUp = stringResource(R.string.maze_move_up)
    val moveDown = stringResource(R.string.maze_move_down)
    val moveLeft = stringResource(R.string.maze_move_left)
    val moveRight = stringResource(R.string.maze_move_right)

    // Shrink cells as the grid grows so it still fits above the controls.
    val cellSize = (320 / size - 4).coerceAtMost(48).dp

    fun move(dr: Int, dc: Int) {
        val newRow = playerRow + dr
        val newCol = playerCol + dc
        if (newRow in maze.indices && newCol in maze[0].indices) {
            if (maze[newRow][newCol] != 1) {
                playerRow = newRow
                playerCol = newCol
                if (maze[newRow][newCol] == 3) {
                    onSolved()
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.maze_title), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(24.dp))

        // Render Maze Grid
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            maze.forEachIndexed { r, row ->
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    row.forEachIndexed { c, cell ->
                        val isPlayer = (r == playerRow && c == playerCol)
                        val bg = when {
                            isPlayer -> MaterialTheme.colorScheme.primary
                            cell == 1 -> MaterialTheme.colorScheme.outline
                            cell == 3 -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Box(
                            modifier = Modifier
                                .size(cellSize)
                                .background(bg, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPlayer) {
                                Box(modifier = Modifier.size(cellSize / 3).background(Color.White, CircleShape))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Directional Controls
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = { move(-1, 0) },
                modifier = Modifier.semantics { contentDescription = moveUp }
            ) { Text("↑") }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Button(
                    onClick = { move(0, -1) },
                    modifier = Modifier.semantics { contentDescription = moveLeft }
                ) { Text("←") }
                Button(
                    onClick = { move(0, 1) },
                    modifier = Modifier.semantics { contentDescription = moveRight }
                ) { Text("→") }
            }
            Button(
                onClick = { move(1, 0) },
                modifier = Modifier.semantics { contentDescription = moveDown }
            ) { Text("↓") }
        }
    }
}