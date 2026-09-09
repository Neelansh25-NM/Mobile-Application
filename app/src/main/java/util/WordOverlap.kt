package com.example.alarmboss.util

/** Percentage of target's words that also appear in heard/typed text (case-insensitive). */
fun wordOverlapPercent(target: String, heard: String): Int {
    val targetWords = target.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }.toSet()
    val heardWords = heard.lowercase().split(Regex("\\W+")).filter { it.isNotBlank() }.toSet()
    if (targetWords.isEmpty()) return 0
    val overlap = targetWords.intersect(heardWords).size
    return (overlap * 100) / targetWords.size
}