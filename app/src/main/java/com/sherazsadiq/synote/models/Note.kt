package com.sherazsadiq.synote.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Note(
    val id: String? = null,
    val title: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val favorite: Boolean = false,
    val userId: String = "",
    val labels: List<String> = emptyList(),
    val tags: List<String> = emptyList()
) : Parcelable

