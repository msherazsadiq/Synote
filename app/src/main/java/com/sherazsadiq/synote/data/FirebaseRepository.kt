package com.sherazsadiq.synote.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sherazsadiq.synote.models.Note
import com.sherazsadiq.synote.utils.ResultState
import kotlinx.coroutines.tasks.await

class FirebaseRepository {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val notesRef = firestore.collection("notes")

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

    fun addNote(note: Note) {
        notesRef.add(note)
    }

    fun updateNote(note: Note) {
        notesRef.document(note.id).set(note)
    }

    fun deleteNote(noteId: String) {
        notesRef.document(noteId).delete()
    }

    fun getUserNotes(userId: String) = notesRef
        .whereEqualTo("userId", userId)
        .orderBy("timestamp", Query.Direction.DESCENDING)
}
