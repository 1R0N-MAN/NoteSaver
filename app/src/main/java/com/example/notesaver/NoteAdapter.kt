package com.example.notesaver

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

val itemPos = arrayListOf<Int>()
var inSelection = false

class NoteAdapter (private val notes: ArrayList<NoteData>): RecyclerView.Adapter<NoteAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View, context: Context): RecyclerView.ViewHolder(itemView){
        val noteTitle: TextView = itemView.findViewById(R.id.note_title)
        val noteDate: TextView = itemView.findViewById(R.id.note_date)
        val note: ConstraintLayout = itemView.findViewById(R.id.note)
        val myContext = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return MyViewHolder(itemView, parent.context)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = notes[position]
        holder.noteTitle.text = currentItem.noteTitle
        holder.noteDate.text = currentItem.date
        holder.note.setOnClickListener { select(holder, position) }
        holder.note.setOnLongClickListener { displayNoteOptions(holder, position) }
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    private fun select(holder: MyViewHolder, position: Int){
        if (inSelection){
            selectItem(holder, position)
        }
        else {
            val item = notes[position]
            val context = holder.myContext
            editNote(item, context)
        }
    }

    private fun editNote(item: NoteData, context: Context){
        // open the EditNoteActivity with the note data
        val intent = Intent(context, EditNoteActivity::class.java)
        intent.apply { putExtra("NoteTitle", item.noteTitle) }
        intent.apply { putExtra("Note", item.note) }
        context.startActivity(intent)
    }

    private fun displayNoteOptions(holder: MyViewHolder, position: Int): Boolean {
        // change the background to light blue
        val green = ContextCompat.getColor(holder.myContext, R.color.pale_green)
        holder.note.setBackgroundColor(green)

        // reduce the alpha of everything
        holder.note.alpha = 0.5F
        holder.noteTitle.alpha = 0.5F
        holder.noteDate.alpha = 0.5F

        // add item position to array of item positions
        itemPos.add(position)

        // change NOTES TextView to n Selected where n is the length of items in itemPos array, if itemPos size is 0, revert back to normal
        val currentItem = notes[position]
        val notesView = currentItem.notesView
        val noOfItemsSelected = "${itemPos.size} Selected"
        notesView.text = noOfItemsSelected

        // change createNote image to trashcan image and change onclick listener to deleteNotes() method
        val createNote = currentItem.createNoteBtn
        createNote.setImageResource(R.drawable.delete)
        createNote.setOnClickListener { deleteNotes(holder) }

        // change onclick listener to selectItem() method
        inSelection = true
        return true
    }

    private fun selectItem(holder: MyViewHolder, position: Int){
        if (holder.note.alpha > 0.5){
            // change the background to light blue
            val green = ContextCompat.getColor(holder.myContext, R.color.pale_green)
            holder.note.setBackgroundColor(green)

            // reduce the alpha of everything
            holder.note.alpha = 0.5F
            holder.noteTitle.alpha = 0.5F
            holder.noteDate.alpha = 0.5F

            // add item position to array of item positions
            itemPos.add(position)

            // change NOTES TextView to n Selected where n is the length of items in itemPos array
            val currentItem = notes[position]
            val notesView = currentItem.notesView
            val noOfItemsSelected = "${itemPos.size} Selected"
            notesView.text = noOfItemsSelected
        }
        else {
            // revert background and alpha
            val transparent = ContextCompat.getColor(holder.myContext, android.R.color.transparent)
            holder.note.setBackgroundColor(transparent)
            holder.note.alpha = 1F
            holder.noteTitle.alpha = 1F
            holder.noteDate.alpha = 1F
            // remove item position from array
            itemPos.remove(position)
            val currentItem = notes[position]
            val notesView = currentItem.notesView
            if (itemPos.size > 0){
                // change NOTES TextView to n Selected where n is the length of items in itemPos array
                val noOfItemsSelected = "${itemPos.size} Selected"
                notesView.text = noOfItemsSelected
            }
            else {
                // change TextView back to NOTES if the number of notes selected is 0
                notesView.text = holder.myContext.resources.getString(R.string.notes)

                // change createNote image to back to previous image
                val createNote = currentItem.createNoteBtn
                createNote.setImageResource(R.drawable.new_task)
                createNote.setOnClickListener { createNote(holder.myContext) }

                // change onclick listener to editNote() method
                inSelection = false
            }
        }
    }

    private fun createNote(context: Context){
        val intent = Intent(context, EditNoteActivity::class.java)
        context.startActivity(intent)
    }

    private fun deleteNotes(holder: MyViewHolder){
        var currentItem = notes[itemPos[0]]
        itemPos.sortDescending()
        Log.d("Logs", "itemPos array: $itemPos")

        for (pos in itemPos){
            Log.d("Logs", "Deleting item at pos $pos")
            currentItem = notes[pos]

            // remove the note title from shared preferences file
            val sh = holder.myContext.getSharedPreferences(noteTitles, Context.MODE_PRIVATE)
            val titles = sh.getString(noteTitles, "")
            val title = currentItem.noteTitle
            Log.d("Logs", "Former list of titles: $titles")
            val newTitles = titles!!.replace("$title$KEY", "")
            Log.d("Logs", "New list of titles: $newTitles")
            val editor = sh.edit()
            editor.putString(noteTitles, newTitles)
            editor.apply()

            // delete item from recycler view using its position and update the recycler view
            notes.removeAt(pos)
            notifyItemRemoved(pos)
            notifyItemRangeChanged(pos, notes.size)

            // delete the txt file using the file name
            holder.myContext.deleteFile("$title.txt")

        }
        // revert everything back to normal (NOTES TextView, trash can button image and onClickListener
        // change TextView back to NOTES
        val notesView = currentItem.notesView
        notesView.text = holder.myContext.resources.getString(R.string.notes)

        // change createNote image to back to previous image
        val createNote = currentItem.createNoteBtn
        createNote.setImageResource(R.drawable.new_task)
        createNote.setOnClickListener { createNote(holder.myContext) }

        // change onclick listener to editNote() method
        inSelection = false

        // remove all view positions from itemPos arraylist
        itemPos.clear()
    }
}