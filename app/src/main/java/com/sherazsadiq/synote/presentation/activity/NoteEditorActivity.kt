package com.sherazsadiq.synote.presentation.activity

import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.sherazsadiq.synote.data.FirebaseRepository
import com.sherazsadiq.synote.databinding.ActivityNoteEditorBinding
import com.sherazsadiq.synote.models.Note
import com.sherazsadiq.synote.presentation.adapter.ChipAdapter
import com.sherazsadiq.synote.utils.ResultState
import kotlinx.coroutines.launch

class NoteEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteEditorBinding
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    private val tagList = mutableListOf<String>()
    private val labelList = mutableListOf<String>()

    private lateinit var tagAdapter: ChipAdapter
    private lateinit var labelAdapter: ChipAdapter

    private val firebaseRepository = FirebaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Initialize adapters early
        tagAdapter = ChipAdapter(tagList) { removeTag(it) }
        labelAdapter = ChipAdapter(labelList) { removeLabel(it) }

        binding.tagsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.labelsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.tagsRecyclerView.adapter = tagAdapter
        binding.labelsRecyclerView.adapter = labelAdapter

        // ✅ Now safe to update adapters from intent data
        val note = intent.getParcelableExtra<Note>("note")
        note?.let {
            binding.titleEditText.setText(it.title)
            binding.descriptionEditText.setText(it.content)

            if (it.tags.isNotEmpty()) {
                tagList.addAll(it.tags)
                tagAdapter.notifyDataSetChanged()
            }
            if (it.labels.isNotEmpty()) {
                labelList.addAll(it.labels)
                labelAdapter.notifyDataSetChanged()
            }
        }


        binding.cancelButton.setOnClickListener { finish() }

        binding.saveButton.setOnClickListener { saveNote() }

        binding.addTagButton.setOnClickListener { showInputDialog("Tag") { tagList.add(it); tagAdapter.notifyItemInserted(tagList.size - 1) } }

        binding.addLabelButton.setOnClickListener { showInputDialog("Label") { labelList.add(it); labelAdapter.notifyItemInserted(labelList.size - 1) } }
    }

    private fun showInputDialog(type: String, onAdd: (String) -> Unit) {
        val input = EditText(this)
        AlertDialog.Builder(this)
            .setTitle("Add $type")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val text = input.text.toString().trim()
                if (text.isNotEmpty()) onAdd(text)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun removeTag(tag: String) {
        tagList.remove(tag)
        tagAdapter.notifyDataSetChanged()
    }

    private fun removeLabel(label: String) {
        labelList.remove(label)
        labelAdapter.notifyDataSetChanged()
    }

    private fun saveNote() {
        val title = binding.titleEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Disable save button to prevent multiple taps
        binding.saveButton.isEnabled = false

        val userId = firebaseRepository.getCurrentUserId()
        if (userId == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            binding.saveButton.isEnabled = true // Re-enable on failure
            return
        }

        val existingNote = intent.getParcelableExtra<Note>("note")
        val noteToSave = Note(
            id = existingNote?.id ?: "",
            title = title,
            content = description,
            userId = userId,
            labels = labelList,
            tags = tagList,
            timestamp = System.currentTimeMillis(),
        )

        lifecycleScope.launch {
            val result = if (existingNote != null) {
                firebaseRepository.updateNote(existingNote.id ?: "", noteToSave)
            } else {
                firebaseRepository.addNote(noteToSave)
            }

            when (result) {
                is ResultState.Success -> {
                    Toast.makeText(this@NoteEditorActivity, "Note saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is ResultState.Error -> {
                    Log.e("SaveNote", "Error: ${result.message}")
                    Toast.makeText(this@NoteEditorActivity, "Failed: ${result.message}", Toast.LENGTH_SHORT).show()
                    binding.saveButton.isEnabled = true // Re-enable on failure
                }
                else -> {
                    binding.saveButton.isEnabled = true
                }
            }
        }
    }


}
