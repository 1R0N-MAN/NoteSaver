package com.example.notesaver

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.StringBuilder

class NotesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        val createNote = findViewById<ImageButton>(R.id.create_note)
        createNote.setOnClickListener { createNote() }

        displayNotes()
    }

    override fun onResume() {
        super.onResume()
        displayNotes()
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        if (inSelection){
            // restart the activity
            overridePendingTransition(0, 0)
            finish()
            overridePendingTransition(0, 0)
            startActivity(intent)
            overridePendingTransition(0, 0)

            // reset variables
            itemPos.clear()
            inSelection = false
        }
        else {
            finish()
        }
    }

    private fun displayNotes(){
        // get the recycler view from the notes activity layout
        val recyclerNotes = findViewById<RecyclerView>(R.id.recycler_notes)

        val notes = getNotes()
        Log.d("Logs", "Notes: $notes")
        recyclerNotes.adapter = NoteAdapter(notes)

        if (notes.isEmpty()){
            Toast.makeText(this, "No Notes Created!", Toast.LENGTH_LONG).show()
        }
    }

    private fun createNote(){
        val intent = Intent(this, EditNoteActivity::class.java)
        startActivity(intent)
    }

    private fun getNotes(): ArrayList<NoteData> {
        // the note is saved in the internal storage with the name as note_title
        // the note contains the date as the first line and the note at the remaining lines
        // there is a SharedPreferences file containing all the note_titles

        // get the list of note_titles
        val sh = getSharedPreferences(noteTitles, MODE_PRIVATE)
        val stringValue = sh.getString(noteTitles, "")
        var noteTitles = listOf<String>()

        if (stringValue != ""){
            noteTitles = stringValue!!.split(KEY)
        }

        // create a new array list of NoteData objects
        val notes = arrayListOf<NoteData>()
        Log.d("Logs", "$noteTitles")

        if (noteTitles.isNotEmpty()){
            for (noteTitle in noteTitles){

                //open each note_title as txt files
                try {
                    val fileInputStream = openFileInput("$noteTitle.txt")
                    val inputStreamReader = InputStreamReader(fileInputStream)
                    val bufferedReader = BufferedReader(inputStreamReader)
                    val stringBuilder = StringBuilder()
                    var text = bufferedReader.readLine()

                    while (text != null){
                        stringBuilder.append("$text\n")
                        text = bufferedReader.readLine()
                    }

                    val data = stringBuilder.toString()
                    fileInputStream.close()

                    // get the first line as the date
                    val date = data.take(10)
                    // get the remaining lines as the note
                    val note = data.drop(11)
                    // use the note_title, date, and note to create a NoteData object
                    // also add the notes textview and the create note image button
                    val notesView = findViewById<TextView>(R.id.notes_textview)
                    val createNote = findViewById<ImageButton>(R.id.create_note)

                    val noteData = NoteData(noteTitle, date, note, notesView, createNote)
                    // add the NoteData object to the array list
                    notes.add(noteData)

                } catch (e: IOException){
                    if (noteTitle != "") { Toast.makeText(this, "Note titled '$noteTitle' not found!", Toast.LENGTH_SHORT).show() }
                }
            }
        }
        else {
            Toast.makeText(this, "No notes found!", Toast.LENGTH_SHORT).show()
        }

        // return the array list at the end of the loop
        return notes
    }
}