package com.hellmund.primetime.onboarding

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.hellmund.primetime.R
import kotlinx.android.synthetic.main.list_item_introduction_bg.view.*

class PostersAdapter2(
        private val requestManager: RequestManager,
        private val posterUrls: List<String>
) : RecyclerView.Adapter<PostersAdapter2.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_introduction_bg, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posterUrls[position], requestManager)
    }

    override fun getItemCount(): Int = posterUrls.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(posterUrl: String, requestManager: RequestManager) {
            val options = RequestOptions()
                    .placeholder(R.drawable.poster_placeholder)
                    .centerCrop()

            requestManager
                    .load(posterUrl)
                    .apply(options)
                    .into(itemView.posterImageView)
        }

    }

}
