package com.sherazsadiq.synote.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.sherazsadiq.synote.models.Note
import com.sherazsadiq.synote.utils.ResultState
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val notesRef = FirebaseFirestore.getInstance().collection("notes")


    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    suspend fun login(email: String, password: String): ResultState<Boolean> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            ResultState.Success(true)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Login failed")
        }
    }

    suspend fun signup(email: String, password: String): ResultState<Boolean> {
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            ResultState.Success(true)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Signup failed")
        }
    }


    fun logout() {
        auth.signOut()
    }

    suspend fun addNote(note: Note): ResultState<Boolean> {
        return try {
            val userId = getCurrentUserId() ?: return ResultState.Error("User not authenticated")

            // Create a note without ID, but with correct userId
            val noteWithUserId = note.copy(userId = userId, id = null)

            // Add the note and get the generated document reference
            val docRef = notesRef.add(noteWithUserId).await()

            // Update the document with the generated ID
            docRef.update("id", docRef.id).await()

            ResultState.Success(true)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Failed to save note")
        }
    }





    suspend fun getAllNotesForUser(userId: String): ResultState<List<Note>> {
        return try {
            val snapshot = notesRef
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val notes = snapshot.documents.mapNotNull { it.toObject(Note::class.java) }

            ResultState.Success(notes)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Failed to fetch notes")
        }
    }


    suspend fun updateNote(noteId: String, updatedNote: Note): ResultState<String> {
        return try {
            val noteMap = hashMapOf(
                "title" to updatedNote.title,
                "content" to updatedNote.content,
                "userId" to updatedNote.userId, // 🔐 required due to Firestore rules
                "labels" to updatedNote.labels,
                "tags" to updatedNote.tags,
                "timestamp" to updatedNote.timestamp
            )

            firestore.collection("notes")
                .document(noteId)
                .set(noteMap, SetOptions.merge()) // ✅ merges with existing note, doesn't overwrite everything
                .await()

            ResultState.Success("Note updated successfully")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Unknown error")
        }
    }


    fun listenToUserNotes(
        userId: String,
        onNotesChanged: (List<Note>) -> Unit
    ): ListenerRegistration {
        return firestore.collection("notes")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onNotesChanged(emptyList())
                    return@addSnapshotListener
                }

                val notes = snapshot?.toObjects(Note::class.java) ?: emptyList()
                onNotesChanged(notes)
            }
    }


    suspend fun deleteNote(noteId: String): ResultState<String> {
        return try {
            firestore.collection("notes").document(noteId).delete().await()
            ResultState.Success("Note deleted")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error deleting note")
        }
    }


    suspend fun toggleFavorite(noteId: String, favorite: Boolean): ResultState<String> {
        return try {
            firestore.collection("notes").document(noteId)
                .update("favorite", favorite).await()
            ResultState.Success("Favorite status updated")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error updating favorite")
        }
    }





}
