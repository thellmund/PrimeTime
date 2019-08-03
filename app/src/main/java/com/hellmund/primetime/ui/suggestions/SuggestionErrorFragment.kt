package com.hellmund.primetime.ui.suggestions

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hellmund.primetime.R
import kotlinx.android.synthetic.main.fragment_movie_suggestion_error.*

class SuggestionErrorFragment : Fragment() {

    private lateinit var onRetry: () -> Unit

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_movie_suggestion_error, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        error_btn.setOnClickListener { onRetry() }
    }

    companion object {
        fun newInstance(onRetry: () -> Unit): SuggestionErrorFragment {
            return SuggestionErrorFragment().apply {
                this.onRetry = onRetry
            }
        }
    }

}
