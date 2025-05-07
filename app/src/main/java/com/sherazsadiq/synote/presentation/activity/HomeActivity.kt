package com.sherazsadiq.synote.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.sherazsadiq.synote.R
import com.sherazsadiq.synote.data.FirebaseRepository
import com.sherazsadiq.synote.databinding.ActivityHomeBinding
import com.sherazsadiq.synote.models.Note
import com.sherazsadiq.synote.presentation.adapter.NoteAdapter
import com.sherazsadiq.synote.utils.ResultState
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private val firebaseRepository = FirebaseRepository()
    private lateinit var noteAdapter: NoteAdapter
    private var notesListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar actions
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_logout -> {
                    getSharedPreferences("SynotePrefs", MODE_PRIVATE)
                        .edit().clear().apply()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        // Setup RecyclerView with delete and favorite toggle functionality
        noteAdapter = NoteAdapter(mutableListOf(), onNoteClick = { note ->
            val intent = Intent(this, NoteEditorActivity::class.java)
            intent.putExtra("note", note)
            startActivity(intent)
        }, onNoteDelete = { note ->
            lifecycleScope.launch {
                val result = firebaseRepository.deleteNote(note.id ?: "")
                if (result is ResultState.Success) {
                    Toast.makeText(this@HomeActivity, "Deleted", Toast.LENGTH_SHORT).show()
                }
            }
        }, onFavoriteToggle = { note ->
            val updatedNote = note.copy(isFavorite = !note.isFavorite)
            val newNotes = noteAdapter.getNotes().map {
                if (it.id == note.id) updatedNote else it
            }
            noteAdapter.updateNotes(newNotes)


    })

        binding.notesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = noteAdapter
        }

        binding.addNoteButton.setOnClickListener {
            startActivity(Intent(this, NoteEditorActivity::class.java))
        }

        // Start listening for real-time note updates
        val userId = firebaseRepository.getCurrentUserId()
        if (userId != null) {
            startListeningToNotes(userId)
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    // Realtime listener for notes
    private fun startListeningToNotes(userId: String) {
        notesListener = firebaseRepository.listenToUserNotes(userId) { notes ->
            noteAdapter.updateNotes(notes)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove Firestore listener to avoid memory leaks
        notesListener?.remove()
    }
}
