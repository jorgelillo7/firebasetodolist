package com.example.jorlilcob.firebasetodo.model

/**
 * Created by jorlilcob on 6/03/18.
 */
class ToDoItem {
    companion object Factory {
        fun create(): ToDoItem = ToDoItem()
    }
    var objectId: String? = null
    var itemText: String? = null
    var done: Boolean? = false
}