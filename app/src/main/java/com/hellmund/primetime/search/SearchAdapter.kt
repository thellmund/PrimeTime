package com.hellmund.primetime.search

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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hellmund.primetime.R
import com.hellmund.primetime.model.SearchResult

class SearchAdapter(
        private val context: Context,
        private val onClick: (SearchResult) -> Unit
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

        holder.bind(context, items[position], onClick)

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

        fun bind(context: Context, searchResult: SearchResult, onClick: (SearchResult) -> Unit) {
            loadImage(context, searchResult)
            title.text = searchResult.title
            description.text = searchResult.description
            similarMoviesButton.setOnClickListener { onClick(searchResult) }
        }

        private fun loadImage(context: Context, searchResult: SearchResult) {
            Glide.with(context)
                    .load(searchResult.fullPosterPath)
                    .apply(RequestOptions()
                            .placeholder(R.color.placeholder_grey)
                            .error(R.color.placeholder_grey)
                    )
                    .into(poster)
        }

        @BindView(R.id.posterImageView)
        lateinit var poster: ImageView

        @BindView(R.id.title)
        lateinit var title: TextView

        @BindView(R.id.description)
        lateinit var description: TextView

        @BindView(R.id.similarMoviesButton)
        lateinit var similarMoviesButton: AppCompatButton

        init {
            ButterKnife.bind(this, view)
        }
    }

}
