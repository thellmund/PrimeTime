package com.hellmund.primetime.ui.suggestions.details

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.R
import kotlinx.android.synthetic.main.list_item_review.view.*

private const val COLLAPSED_LINES = 3

class ReviewsAdapter: RecyclerView.Adapter<ReviewsAdapter.ViewHolder>() {

    private val reviews = mutableListOf<Review>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount(): Int = reviews.size

    fun update(newReviews: List<Review>) {
        reviews.clear()
        reviews += newReviews
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(review: Review) = with(itemView) {
            // TODO: Markdown support
            /*Markwon.create(context).apply {
                val content = render(parse(review.content))
                setParsedMarkdown(reviewTextView, content)
            }*/

            reviewTextView.text = review.content
            setOnClickListener {
                if (reviewTextView.maxLines == COLLAPSED_LINES) {
                    reviewTextView.maxLines = Int.MAX_VALUE
                    reviewTextView.ellipsize = null
                } else {
                    reviewTextView.maxLines = COLLAPSED_LINES
                    reviewTextView.ellipsize = TextUtils.TruncateAt.END
                }
            }
        }

    }

}
