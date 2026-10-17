package com.example.alarmboss.ui.ringing.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.alarmboss.R

@Composable
fun WordScrambleTaskScreen(difficulty: Int, onSolved: () -> Unit) {
    val wordCount = (difficulty / 2) + 1
    
    val wordList = remember {
        listOf(
            "MORNING", "WAKEUP", "ALARM", "SUNRISE", "COFFEE", "ENERGY", 
            "BREAKFAST", "STRETCH", "ROUTINE", "SUCCESS", "FOCUS", "READY"
        )
    }
    
    var solvedCount by remember { mutableIntStateOf(0) }
    var currentWord by remember { mutableStateOf(wordList.random()) }
    var scrambledWord by remember { mutableStateOf(currentWord.toList().shuffled().joinToString("")) }
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    // Ensure scramble isn't identical to word
    LaunchedEffect(currentWord) {
        var scramble = currentWord.toList().shuffled().joinToString("")
        while (scramble == currentWord) {
            scramble = currentWord.toList().shuffled().joinToString("")
        }
        scrambledWord = scramble
        input = ""
        error = false
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(pluralStringResource(R.plurals.scramble_title, wordCount, wordCount), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.task_progress, solvedCount, wordCount), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(32.dp))
        
        Text(
            text = scrambledWord, 
            style = MaterialTheme.typography.displayMedium,
            letterSpacing = MaterialTheme.typography.headlineMedium.letterSpacing
        )
        
        Spacer(Modifier.height(32.dp))
        
        OutlinedTextField(
            value = input,
            onValueChange = { 
                input = it.uppercase().filter { char -> char.isLetter() }
                error = false
            },
            label = { Text(stringResource(R.string.task_answer)) },
            singleLine = true,
            isError = error,
            modifier = Modifier.fillMaxWidth(0.8f)
        )
        
        if (error) {
            Text(stringResource(R.string.task_try_again), color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
        
        Spacer(Modifier.height(24.dp))
        
        Button(onClick = {
            if (input == currentWord) {
                solvedCount++
                if (solvedCount >= wordCount) {
                    onSolved()
                } else {
                    // Never repeat the word just solved: the reset effect keys on the word, so a
                    // repeat would leave the correct answer sitting in the box.
                    input = ""
                    currentWord = wordList.filter { it != currentWord }.random()
                }
            } else {
                error = true
            }
        }) {
            Text(stringResource(R.string.task_submit))
        }
    }
}
