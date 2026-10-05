package com.example.alarmboss.ui.productivity

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductivityScreen() {
    var queryText by remember { mutableStateOf("") }
    var responseText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()


    val apiKey = "AQ.Ab8RN6KM_sf86U9nR1MvwhdPWS33x6fMfjDO-hc7pPd5Pg_J4w"


    val generativeModel = remember {
        GenerativeModel(
            modelName = "gemini-3.5-flash",
            apiKey = apiKey,
            systemInstruction = content {
                text(
                    "YOU are an AI advisor for an AI powered alarm app called Alarm Boss. " +
                            "Your job is to strictly make sure that users get responses ONLY about queries " +
                            "pertaining to deep sleep techniques, feeling fully rested, preventing sleep inertia, " +
                            "getting up exactly on time, preventing grogginess, how to handle lethargy, and immediate productivity. " +
                            "Do not mention that you are an AI from Google or Gemini. " +
                            "If the user asks about anything unrelated (e.g., coding, history, casual chat, math, entertainment), " +
                            "you must firmly but politely refuse to answer, tell them to stop procrastinating, and pivot back to sleep/productivity."
                )
            }
        )
    }

    fun askAdvisor() {
        if (queryText.isBlank()) return

        if (apiKey != "YOUR_GEMINI_API_KEY") {
            coroutineScope.launch {
                isLoading = true
                responseText = ""
                try {
                    val response = generativeModel.generateContent(queryText)
                    responseText = response.text ?: "No response generated."
                } catch (e: Exception) {
                    responseText = "Connection error. Get some sleep! (${e.localizedMessage})"
                } finally {
                    isLoading = false
                }
            }
        } else {
            responseText = "Please insert your API Key in the code to use the Alarm Boss Advisor."
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Productivity & Sleep Hacks") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Slogan
            Text(
                text = "⚡ \"Discipline is choosing between what you want now and what you want most.\"",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = Color(0xFFFFA500),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Distraction-Free Input
            OutlinedTextField(
                value = queryText,
                onValueChange = { queryText = it },
                label = { Text("Ask about sleep or wake-up hacks...") },
                trailingIcon = {
                    IconButton(onClick = { askAdvisor() }) {
                        Icon(Icons.Default.Send, contentDescription = "Ask Advisor")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Custom Branded Advisor Response
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else if (responseText.isNotEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🧠 Alarm Boss Advisor",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = responseText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}