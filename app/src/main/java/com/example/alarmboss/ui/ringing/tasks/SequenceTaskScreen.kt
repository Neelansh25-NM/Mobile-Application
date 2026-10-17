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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.alarmboss.R
import kotlinx.coroutines.delay

@Composable
fun SequenceTaskScreen(difficulty: Int, onSolved: () -> Unit) {
    val sequenceLength = (difficulty / 2) + 3 // Starts at 3, scales up
    val gridSize = 4 // 2x2 grid is standard for Simon Says
    
    var sequence by remember { mutableStateOf(emptyList<Int>()) }
    var playerInput by remember { mutableStateOf(emptyList<Int>()) }
    var activeTile by remember { mutableStateOf<Int?>(null) }
    var isShowingSequence by remember { mutableStateOf(true) }
    var errorState by remember { mutableStateOf(false) }
    var resetTrigger by remember { mutableIntStateOf(0) }

    LaunchedEffect(resetTrigger, difficulty) {
        isShowingSequence = true
        errorState = false
        playerInput = emptyList()
        
        val newSeq = List(sequenceLength) { (0 until gridSize).random() }
        sequence = newSeq
        
        delay(1000)
        
        val flashDuration = (800L - (difficulty * 50L)).coerceAtLeast(200L)
        val pauseDuration = flashDuration / 2
        
        for (tile in sequence) {
            activeTile = tile
            delay(flashDuration)
            activeTile = null
            delay(pauseDuration)
        }
        
        isShowingSequence = false
    }

    LaunchedEffect(playerInput) {
        if (!isShowingSequence && playerInput.isNotEmpty()) {
            val currentIndex = playerInput.size - 1
            if (playerInput[currentIndex] != sequence[currentIndex]) {
                errorState = true
                delay(800)
                resetTrigger++
            } else if (playerInput.size == sequence.size) {
                delay(300)
                onSolved()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(if (isShowingSequence) R.string.sequence_watch else R.string.sequence_repeat),
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.task_progress, playerInput.size, sequenceLength),
            style = MaterialTheme.typography.bodyMedium,
            color = if (errorState) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.aspectRatio(1f)
        ) {
            items(gridSize) { index ->
                val isActive = index == activeTile
                
                val tileColor = when {
                    errorState && playerInput.lastOrNull() == index -> MaterialTheme.colorScheme.error
                    isActive -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(tileColor)
                        .clickable(enabled = !isShowingSequence && !errorState) {
                            activeTile = index
                            playerInput = playerInput + index
                        }
                )
            }
        }
        
        // Reset active tile on manual tap to create flash effect
        LaunchedEffect(activeTile) {
            if (!isShowingSequence && activeTile != null && !errorState) {
                delay(200)
                activeTile = null
            }
        }
    }
}
