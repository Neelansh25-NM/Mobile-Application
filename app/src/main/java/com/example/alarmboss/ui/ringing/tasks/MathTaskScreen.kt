package com.example.alarmboss.ui.ringing.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private data class Problem(val text: String, val answer: Int)

private fun randomProblem(): Problem {
    val a = Random.nextInt(2, 50)
    val b = Random.nextInt(2, 50)
    return when (Random.nextInt(3)) {
        0 -> Problem("$a + $b", a + b)
        1 -> Problem("$a - $b", a - b)
        else -> {
            val small = Random.nextInt(2, 12)
            val small2 = Random.nextInt(2, 12)
            Problem("$small × $small2", small * small2)
        }
    }
}

/** Requires solving 3 problems in a row correctly to dismiss the alarm. */
@Composable
fun MathTaskScreen(onSolved: () -> Unit) {
    var solvedCount by remember { mutableIntStateOf(0) }
    var problem by remember { mutableStateOf(randomProblem()) }
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    val target = 3

    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Solve $target problems to dismiss", style = MaterialTheme.typography.titleMedium)
        Text("$solvedCount / $target", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(24.dp))
        Text(problem.text, style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = input,
            onValueChange = { input = it.filter { c -> c.isDigit() || c == '-' }; error = false },
            label = { Text("Answer") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = error
        )
        if (error) Text("Try again", color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            if (input.toIntOrNull() == problem.answer) {
                solvedCount++
                input = ""
                if (solvedCount >= target) onSolved() else problem = randomProblem()
            } else {
                error = true
            }
        }) { Text("Submit") }
    }
}
