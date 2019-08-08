package com.hellmund.primetime.ui.suggestions.details

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.primetime.R
import kotlinx.android.synthetic.main.list_item_review.view.reviewTextView

private const val COLLAPSED_LINES = 3

class ReviewsAdapter : ListAdapter<Review, ReviewsAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<Review>() {
        override fun areItemsTheSame(
            oldItem: Review,
            newItem: Review
        ) = oldItem.id == newItem.id

        override fun areContentsTheSame(
            oldItem: Review,
            newItem: Review
        ) = oldItem == newItem
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(review: Review) = with(itemView) {
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
