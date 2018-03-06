package com.example.jorlilcob.firebasetodo.interfaces

/**
 * Created by jorlilcob on 6/03/18.
 */
interface ItemRowListener {
    fun modifyItemState(itemObjectId: String, isDone: Boolean)
    fun onItemDelete(itemObjectId: String)
}