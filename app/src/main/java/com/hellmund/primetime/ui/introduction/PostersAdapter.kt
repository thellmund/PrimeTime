package com.hellmund.primetime.ui.introduction

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellmund.primetime.R
import com.hellmund.primetime.utils.ImageLoader
import com.hellmund.primetime.utils.Transformation
import kotlinx.android.synthetic.main.list_item_introduction_bg.view.*

class PostersAdapter(
        private val imageLoader: ImageLoader,
        private val posterUrls: List<String>
) : RecyclerView.Adapter<PostersAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_introduction_bg, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posterUrls[position], imageLoader)
    }

    override fun getItemCount(): Int = posterUrls.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(posterUrl: String, imageLoader: ImageLoader) {
            imageLoader.load(
                    url = posterUrl,
                    into = itemView.posterImageView,
                    transformations = arrayOf(
                        Transformation.Placeholder(R.drawable.poster_placeholder),
                        Transformation.CenterCrop
                    )
            )
        }

    }

}
