package com.sherazsadiq.synote.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sherazsadiq.synote.data.FirebaseRepository
import com.sherazsadiq.synote.models.Note
import com.sherazsadiq.synote.utils.ResultState
import kotlinx.coroutines.launch

class NoteViewModel : ViewModel() {
    private val firebaseRepository = FirebaseRepository()

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> get() = _notes

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    fun getCurrentUserId(): String? = firebaseRepository.getCurrentUserId()

    fun startListeningToNotes(userId: String) {
        firebaseRepository.listenToUserNotes(userId) { fetchedNotes ->
            _notes.postValue(fetchedNotes)
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            val result = firebaseRepository.deleteNote(noteId)
            if (result is ResultState.Success) {
                _toastMessage.value = "Deleted"
            } else {
                _toastMessage.value = "Delete failed"
            }
        }
    }

    fun toggleFavorite(noteId: String, favorite: Boolean) {
        viewModelScope.launch {
            val result = firebaseRepository.toggleFavorite(noteId, favorite)
            if (result is ResultState.Success) {
                _toastMessage.value = "Favorite toggled"
                // Reload notes after the update
                getCurrentUserId()?.let { startListeningToNotes(it) }
            } else {
                _toastMessage.value = "Failed to toggle favorite"
            }
        }
    }
}