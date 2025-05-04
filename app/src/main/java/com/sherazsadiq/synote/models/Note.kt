package com.sherazsadiq.synote.models

data class Note(
    val id: String = "",                         // Firestore Document ID
    val title: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isFavourite: Boolean = false,
    val userId: String = "",                     // Used in Firestore rules
    val labels: List<String> = emptyList(),      // Optional: e.g., ["Work", "Ideas"]
    val tags: List<String> = emptyList()         // Optional: e.g., ["urgent", "2025"]

)
