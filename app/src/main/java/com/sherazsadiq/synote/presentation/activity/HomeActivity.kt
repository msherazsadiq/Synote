package com.sherazsadiq.synote.presentation.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.sherazsadiq.synote.R
import com.sherazsadiq.synote.databinding.ActivityHomeBinding
import com.sherazsadiq.synote.models.Note
import com.sherazsadiq.synote.presentation.adapter.NoteAdapter
import com.sherazsadiq.synote.presentation.viewmodel.NoteViewModel
import android.text.Editable
import android.text.TextWatcher
import com.sherazsadiq.synote.presentation.viewmodel.AuthViewModel


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var noteAdapter: NoteAdapter

    private val noteViewModel by viewModels<NoteViewModel>()


    private var allNotes: List<Note> = emptyList()

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

        // Setup RecyclerView
        noteAdapter = NoteAdapter(mutableListOf(), onNoteClick = { note ->
            val intent = Intent(this, NoteEditorActivity::class.java)
            intent.putExtra("note", note)
            startActivity(intent)
        }, onNoteDelete = { note ->
            noteViewModel.deleteNote(note.id ?: "")
        }, onFavoriteToggle = { note, position ->
            noteViewModel.toggleFavorite(note.id ?: "", !note.favorite)
            val updatedNote = note.copy(favorite = !note.favorite)
            noteAdapter.updateNoteAtPosition(position, updatedNote)
        })

        binding.notesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = noteAdapter
        }

        binding.addNoteButton.setOnClickListener {
            startActivity(Intent(this, NoteEditorActivity::class.java))
        }

        // Observe notes from ViewModel
        noteViewModel.notes.observe(this, Observer { notes ->
            noteAdapter.updateNotes(notes)
        })

        // Observe toast messages
        noteViewModel.toastMessage.observe(this, Observer { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        })

        // Start listening for notes
        val userId = noteViewModel.getCurrentUserId()
        if (userId != null) {
            noteViewModel.startListeningToNotes(userId)
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }

        binding.main.setOnTouchListener { _, _ ->
            binding.searchEditText.clearFocus()
            false
        }

        noteViewModel.notes.observe(this, Observer { notes ->
            allNotes = notes // Store all notes for filtering
            noteAdapter.updateNotes(notes)
        })

        binding.main.setOnTouchListener { _, _ ->
            binding.searchEditText.clearFocus()
            false
        }

        // Add TextWatcher for real-time search
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterNotes(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterNotes(query: String) {
        val filteredNotes = allNotes.filter { note ->
            note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true) ||
                    note.labels.any { it.contains(query, ignoreCase = true) } ||
                    note.tags.any { it.contains(query, ignoreCase = true) }
        }
        noteAdapter.updateNotes(filteredNotes)
    }
}