package com.example.notesaver

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.cardview.widget.CardView
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val KEY = "<d;2'4iv/a?2fsa402`>"
const val noteTitles = "NoteTitles"

class EditNoteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        val saveNote = findViewById<ImageButton>(R.id.save_note)
        saveNote.setOnClickListener { save() }

        setEditValues()
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        val saveNoteDialog = Dialog(this)
        saveNoteDialog.setContentView(R.layout.save_note_popup)
        saveNoteDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val cancel = saveNoteDialog.findViewById<Button>(R.id.dont_save_btn)
        cancel.setOnClickListener { finish() }
        val save = saveNoteDialog.findViewById<Button>(R.id.save_btn)
        save.setOnClickListener { save() }

        saveNoteDialog.show()
    }

    private fun setEditValues(){
        // get the intent that started the activity and extract the data
        val noteTitle = intent.getStringExtra("NoteTitle")
        val note = intent.getStringExtra("Note")

        // get the views for the values
        val noteTitleView = findViewById<EditText>(R.id.note_title)
        val noteText = findViewById<EditText>(R.id.note_text)

        // set the values to default value
        if (noteTitle != null){
            noteTitleView.setText(noteTitle)
        }
        if (note != null){
            noteText.setText(note)
        }
    }

    private fun save(){
        // get the text in note title and note text views
        val noteTitle = findViewById<EditText>(R.id.note_title)
        val noteText = findViewById<EditText>(R.id.note_text)
        val title = noteTitle.text.toString()
        val note = noteText.text.toString()

        // get the current date
        val date = getDate()

        // check if the note title already exists, if it does show a warning
        val sh = getSharedPreferences(noteTitles, MODE_PRIVATE)

        val titles = sh.getString(noteTitles, "")

        if (titles != ""){
            val titlesSet = titles!!.split(KEY).toSet()

            if (titlesSet.contains(title)){
                showWarning(sh, titles, title, date, note)
            } else {
                saveNote(sh, titles, title, date, note)
            }
        } else {
            saveNote(sh, titles, title, date, note)
        }
    }

    private fun getDate(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        return current.format(formatter)
    }

    private fun showWarning(sh: SharedPreferences, titles: String, title:String, date:String, note:String){
        val warningDialog = Dialog(this)
        warningDialog.setContentView(R.layout.warning_layout)
        warningDialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // get the yes and no buttons
        val noBtn = warningDialog.findViewById<CardView>(R.id.no_button)
        noBtn.setOnClickListener { warningDialog.dismiss() }
        val yesBtn = warningDialog.findViewById<CardView>(R.id.yes_button)
        yesBtn.setOnClickListener { dialogYes(warningDialog, sh, titles, title, date, note) }

        // show the dialog
        warningDialog.show()
    }

    private fun dialogYes(warningDialog: Dialog, sh: SharedPreferences, titles: String, title:String, date:String, note:String){

        warningDialog.dismiss()

        // remove the title from the list of titles before saving to prevent the same note from
        val newTitles = titles.replace("$title$KEY", "")

        saveNote(sh, newTitles, title, date, note)
    }

    private fun saveNote(sh: SharedPreferences, titles: String, title:String, date:String, note:String){
        // add note title to SharedPreferences file
        val titlesEdited = "$title$KEY$titles"
        val editor = sh.edit()
        editor.putString(noteTitles, titlesEdited)
        editor.apply()

        try {
            // create a txt file with the note title as the name
            val fileOutputStream = openFileOutput("$title.txt", MODE_PRIVATE)
            // save the string "$date\n$note" in the txt file
            val data = "$date $note"
            Log.d("Logs", "Data: $data")
            Log.d("Logs", "Data converted: ${data.toByteArray()}")
            fileOutputStream.write(data.toByteArray())
            fileOutputStream.flush()
            fileOutputStream.close()
            // close the activity
            finish()

        } catch (e: IOException){
            Toast.makeText(this, "Error: $e", Toast.LENGTH_SHORT).show()
        }
    }
}