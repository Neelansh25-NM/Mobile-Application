package com.example.alarmboss.ui.ringing.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun MemoryTaskScreen(onSolved: () -> Unit) {
    val gridSize = 9 // 3x3 grid
    val targetCount = 4 // Number of tiles to memorize

    var targetTiles by remember { mutableStateOf(emptySet<Int>()) }
    var selectedTiles by remember { mutableStateOf(emptySet<Int>()) }
    var isMemorizePhase by remember { mutableStateOf(true) }
    var resetTrigger by remember { mutableIntStateOf(0) }
    var errorState by remember { mutableStateOf(false) }

    // Generate puzzle and handle the memorization timer
    LaunchedEffect(resetTrigger) {
        isMemorizePhase = true
        errorState = false
        selectedTiles = emptySet()

        val newTargets = mutableSetOf<Int>()
        while (newTargets.size < targetCount) {
            newTargets.add((0 until gridSize).random())
        }
        targetTiles = newTargets

        // Show the pattern to the user for 2 seconds
        delay(2000)
        isMemorizePhase = false
    }

    // Evaluate the user's input once they've tapped enough tiles
    LaunchedEffect(selectedTiles) {
        if (!isMemorizePhase && selectedTiles.size == targetCount) {
            if (selectedTiles == targetTiles) {
                delay(300) // Brief pause so they see their final tap
                onSolved()
            } else {
                errorState = true
                delay(800) // Show error briefly before resetting
                resetTrigger++ // Trigger a new puzzle
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isMemorizePhase) "Memorize the pattern" else "Tap the highlighted tiles",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${selectedTiles.size} / $targetCount",
            style = MaterialTheme.typography.bodyMedium,
            color = if (errorState) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.aspectRatio(1f)
        ) {
            items(gridSize) { index ->
                val isTarget = index in targetTiles
                val isSelected = index in selectedTiles

                val tileColor = when {
                    isMemorizePhase && isTarget -> MaterialTheme.colorScheme.primary
                    !isMemorizePhase && isSelected -> if (errorState && !isTarget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(tileColor)
                        .clickable(
                            enabled = !isMemorizePhase && !isSelected && selectedTiles.size < targetCount
                        ) {
                            selectedTiles = selectedTiles + index
                        }
                )
            }
        }
    }
}