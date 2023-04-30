package com.example.notesaver

import android.widget.ImageButton
import android.widget.TextView

data class NoteData(val noteTitle: String, val date: String, val note: String, val notesView: TextView, val createNoteBtn: ImageButton)
