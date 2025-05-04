package com.sherazsadiq.synote.presentation.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.sherazsadiq.synote.R
import com.sherazsadiq.synote.databinding.ActivityNoteEditorBinding

class NoteEditorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoteEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityNoteEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.cancelButton.setOnClickListener { finish() }


    }
}