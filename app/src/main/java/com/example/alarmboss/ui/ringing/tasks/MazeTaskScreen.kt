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

@Composable
fun MazeTaskScreen(onSolved: () -> Unit) {
    // 0 = path, 1 = wall, 2 = start, 3 = exit
    val maze = remember {
        arrayOf(
            intArrayOf(2, 0, 1, 1, 1),
            intArrayOf(1, 0, 0, 0, 1),
            intArrayOf(1, 1, 1, 0, 1),
            intArrayOf(1, 0, 0, 0, 3),
            intArrayOf(1, 1, 1, 1, 1)
        )
    }

    var playerRow by remember { mutableIntStateOf(0) }
    var playerCol by remember { mutableIntStateOf(0) }

    // Find initial start position
    LaunchedEffect(Unit) {
        for (r in maze.indices) {
            for (c in maze[r].indices) {
                if (maze[r][c] == 2) {
                    playerRow = r
                    playerCol = c
                }
            }
        }
    }

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
        Text("Navigate the maze to dismiss alarm", style = MaterialTheme.typography.titleMedium)
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
                                .size(48.dp)
                                .background(bg, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isPlayer) {
                                Box(modifier = Modifier.size(16.dp).background(Color.White, CircleShape))
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        // Directional Controls
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { move(-1, 0) }) { Text("↑") }
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Button(onClick = { move(0, -1) }) { Text("←") }
                Button(onClick = { move(0, 1) }) { Text("→") }
            }
            Button(onClick = { move(1, 0) }) { Text("↓") }
        }
    }
}