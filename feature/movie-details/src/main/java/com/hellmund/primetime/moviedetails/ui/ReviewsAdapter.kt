package com.hellmund.primetime.moviedetails.ui

import android.text.TextUtils.TruncateAt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hellmund.api.model.Review
import com.hellmund.primetime.moviedetails.R
import com.hellmund.primetime.moviedetails.databinding.ListItemReviewBinding

private const val COLLAPSED_LINES = 3

class ReviewsAdapter : RecyclerView.Adapter<ReviewsAdapter.ViewHolder>() {

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

        private val binding = ListItemReviewBinding.bind(itemView)

        fun bind(review: Review) = with(binding) {
            reviewTextView.text = review.content
            root.setOnClickListener {
                if (reviewTextView.maxLines == COLLAPSED_LINES) {
                    reviewTextView.maxLines = Int.MAX_VALUE
                    reviewTextView.ellipsize = null
                } else {
                    reviewTextView.maxLines = COLLAPSED_LINES
                    reviewTextView.ellipsize = TruncateAt.END
                }
            }
        }
    }
}
