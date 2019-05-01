package com.hellmund.primetime.ui.search

import android.content.Context
import android.support.v7.widget.AppCompatButton
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.hellmund.primetime.R
import com.hellmund.primetime.data.model.SearchResult
import com.hellmund.primetime.utils.ImageLoader

class SearchAdapter(
        private val context: Context,
        private val onShowSimilar: (SearchResult) -> Unit,
        private val onWatched: (SearchResult) -> Unit
) : BaseAdapter() {

    private val items = mutableListOf<SearchResult>()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val holder: ViewHolder

        if (view == null) {
            view = LayoutInflater.from(context)
                    .inflate(R.layout.list_item_search_results, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }

        holder.bind(context, items[position], onShowSimilar, onWatched)

        return view!!
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(position: Int): Any {
        return items[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun update(newItems: List<SearchResult>) {
        items.clear()
        items += newItems
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) {

        fun bind(
                context: Context,
                searchResult: SearchResult,
                onShowSimilar: (SearchResult) -> Unit,
                onWatchedIt: (SearchResult) -> Unit
        ) {
            loadImage(context, searchResult)
            title.text = searchResult.title
            description.text = searchResult.description
            similarMoviesButton.setOnClickListener { onShowSimilar(searchResult) }
            watchedItButton.setOnClickListener { onWatchedIt(searchResult) }

        }

        private fun loadImage(context: Context, searchResult: SearchResult) {
            ImageLoader.with(context).load(searchResult.fullPosterPath, poster)
        }

        @BindView(R.id.posterImageView)
        lateinit var poster: ImageView

        @BindView(R.id.title)
        lateinit var title: TextView

        @BindView(R.id.description)
        lateinit var description: TextView

        @BindView(R.id.similarMoviesButton)
        lateinit var similarMoviesButton: AppCompatButton

        @BindView(R.id.watchedItButton)
        lateinit var watchedItButton: AppCompatButton

        init {
            ButterKnife.bind(this, view)
        }
    }

}
