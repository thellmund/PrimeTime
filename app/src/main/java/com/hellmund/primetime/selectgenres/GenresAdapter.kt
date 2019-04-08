package com.hellmund.primetime.selectgenres

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckedTextView
import com.hellmund.primetime.R
import com.hellmund.primetime.model.Genre

class GenresAdapter @JvmOverloads constructor(
        context: Context,
        private val items: List<Genre>,
        resource: Int = R.layout.list_item_multiple_choice
) : ArrayAdapter<Genre>(context, resource, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
                .inflate(android.R.layout.simple_list_item_multiple_choice, parent, false)

        val textView = view.findViewById<CheckedTextView>(android.R.id.text1)
        textView.text = items[position].name
        textView.setTextColor(Color.WHITE)

        return view
    }

}
