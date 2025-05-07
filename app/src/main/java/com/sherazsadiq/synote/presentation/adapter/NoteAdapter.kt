package com.sherazsadiq.synote.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sherazsadiq.synote.databinding.ItemNoteBinding
import com.sherazsadiq.synote.models.Note
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteAdapter(
    private val notes: MutableList<Note>,
    private val onNoteClick: (Note) -> Unit,
    private val onNoteDelete: (Note) -> Unit,
    private val onFavoriteToggle: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    inner class NoteViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.titleText.text = note.title
            binding.contentText.text = note.content

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            binding.dateText.text = sdf.format(Date(note.timestamp))

            // Show/hide star icon
            binding.favIcon.visibility = if (note.isFavorite) View.VISIBLE else View.GONE

            // Set button label based on isFavorite
            binding.favoriteButton.text = if (note.isFavorite) "Remove Favorite" else "Add to Favorite"

            binding.buttonContainer.visibility = View.GONE

            binding.root.setOnClickListener { onNoteClick(note) }

            binding.root.setOnLongClickListener {
                binding.buttonContainer.visibility = View.VISIBLE
                true
            }

            binding.deleteButton.setOnClickListener {
                onNoteDelete(note)
            }

            binding.favoriteButton.setOnClickListener {
                onFavoriteToggle(note)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount(): Int = notes.size

    fun getNotes(): List<Note> = notes

    fun updateNotes(newNotes: List<Note>) {
        notes.clear()
        notes.addAll(newNotes)
        notifyDataSetChanged()
    }
}
