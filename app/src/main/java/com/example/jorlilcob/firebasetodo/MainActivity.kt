package com.example.jorlilcob.firebasetodo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.EditText
import android.support.design.widget.FloatingActionButton
import android.util.Log
import android.widget.ListView
import android.widget.Toast
import com.example.jorlilcob.firebasetodo.interfaces.ItemRowListener
import com.example.jorlilcob.firebasetodo.model.ToDoItem
import com.google.firebase.database.*

class MainActivity : AppCompatActivity(), ItemRowListener {
    //Get Access to Firebase database, no need of any URL, Firebase
    //identifies the connection via the package name of the app
    lateinit var mDatabase: DatabaseReference
    var toDoItemList: MutableList<ToDoItem>? = null
    lateinit var adapter: ToDoItemAdapter
    private var listViewItems: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //reference for FAB
        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        listViewItems = findViewById<View>(R.id.items_list) as ListView
        //Adding click listener for FAB
        fab.setOnClickListener { view ->
            //Show Dialog here to add new Item
            addNewItemDialog()
        }
        mDatabase = FirebaseDatabase.getInstance().reference
        toDoItemList = mutableListOf<ToDoItem>()
        adapter = ToDoItemAdapter(this, toDoItemList!!)
        listViewItems!!.setAdapter(adapter)
        mDatabase.orderByKey().addListenerForSingleValueEvent(itemListener)


    }

    private fun addNewItemDialog() {
        val alert = AlertDialog.Builder(this)
        val itemEditText = EditText(this)
        alert.setMessage("Add New Firebase Item")
        alert.setTitle("Enter To Do Item Text")
        alert.setView(itemEditText)
        alert.setPositiveButton("Submit") { dialog, positiveButton ->
            val todoItem = ToDoItem.create()
            todoItem.itemText = itemEditText.text.toString()
            todoItem.done = false
            //We first make a push so that a new item is made with a unique ID
            val newItem = mDatabase.child(Constants.FIREBASE_ITEM).push()
            todoItem.objectId = newItem.key
            //then, we used the reference to set the value on that ID
            newItem.setValue(todoItem)

            toDoItemList!!.add(todoItem)
            adapter.notifyDataSetChanged()
            dialog.dismiss()
            Toast.makeText(this, "Item saved with ID " + todoItem.objectId, Toast.LENGTH_SHORT).show()
        }
        alert.show()
    }


    var itemListener: ValueEventListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            // Get Post object and use the values to update the UI
            addDataToList(dataSnapshot)
        }
        override fun onCancelled(databaseError: DatabaseError) {
            // Getting Item failed, log a message
            Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
        }
    }
    private fun addDataToList(dataSnapshot: DataSnapshot) {
        val items = dataSnapshot.children.iterator()
        //Check if current database contains any collection
        if (items.hasNext()) {
            val toDoListindex = items.next()
            val itemsIterator = toDoListindex.children.iterator()

            //check if the collection has any to do items or not
            while (itemsIterator.hasNext()) {
                //get current item
                val currentItem = itemsIterator.next()
                val todoItem = ToDoItem.create()
                //get current data in a map
                val map = currentItem.getValue() as HashMap<String, Any>
                //key will return Firebase ID
                todoItem.objectId = currentItem.key
                todoItem.done = map.get("done") as Boolean?
                todoItem.itemText = map.get("itemText") as String?
                toDoItemList!!.add(todoItem)
            }
        }
        //alert adapter that has changed
        adapter.notifyDataSetChanged()
    }

    override fun modifyItemState(itemObjectId: String, isDone: Boolean) {
        val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
        itemReference.child("done").setValue(isDone)
        toDoItemList!!.forEachIndexed { index: Int, i ->
            if(i.objectId == itemObjectId){
                i.done = isDone
            }
        }

        adapter.notifyDataSetChanged()
    }
    //delete an item
    override fun onItemDelete(itemObjectId: String) {
        //get child reference in database via the ObjectID
        val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
        //deletion can be done via removeValue() method
        itemReference.removeValue()

        var indexToRemove = -1
        toDoItemList!!.forEachIndexed { index: Int, i ->
            if(i.objectId == itemObjectId){
                indexToRemove = index
            }
        }

        if(indexToRemove != -1)
            toDoItemList!!.removeAt(indexToRemove)

        adapter.notifyDataSetChanged()
    }

}