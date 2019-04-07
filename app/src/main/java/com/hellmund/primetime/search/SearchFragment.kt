package com.hellmund.primetime.search

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.hellmund.primetime.R
import com.hellmund.primetime.main.MainActivity
import com.hellmund.primetime.main.MainFragment
import com.hellmund.primetime.model.SearchResult
import com.hellmund.primetime.search.SearchActivity.*
import com.hellmund.primetime.utils.Constants
import com.hellmund.primetime.utils.GenreUtils
import com.hellmund.primetime.utils.UiUtils
import kotlinx.android.synthetic.main.fragment_search.*
import kotlinx.android.synthetic.main.state_layout_search_results.*
import kotlinx.android.synthetic.main.view_search_field.*
import org.jetbrains.anko.inputMethodManager
import java.util.*

class SearchFragment : Fragment(), TextView.OnEditorActionListener, TextWatcher, MainActivity.Reselectable {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSearch()
        initCategoriesRecyclerView()
        initSearchResultsRecyclerView()

        val searchIntent = arguments?.getString(KEY_SEARCH_INTENT)
        searchIntent?.let {
            handleSearchIntent(it)
        }
    }

    private fun handleSearchIntent(searchIntent: String) {
        val fragment = MainFragment.newInstance(searchIntent)
        showFragment(fragment)
    }

    private fun showFragment(fragment: Fragment) {
        requireFragmentManager()
                .beginTransaction()
                .replace(R.id.contentFrame, fragment)
                .addToBackStack(null)
                .commit()
    }

    private fun initSearch() {
        search_box.setOnEditorActionListener(this)
        search_box.addTextChangedListener(this)
        search_box.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchResultsContainer.visibility = View.VISIBLE
                categoriesRecyclerView.visibility = View.GONE
            }
        }

        search_clear.setOnClickListener {
            if (it.alpha == Constants.ENABLED) {
                clearSearchBarContent()
                toggleKeyboard(true)
            }
        }
    }

    private fun initCategoriesRecyclerView() {
        val adapter = SearchCategoriesAdapter(buildCategories(), this::onCategorySelected)
        categoriesRecyclerView.itemAnimator = DefaultItemAnimator()
        categoriesRecyclerView.adapter = adapter
        categoriesRecyclerView.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
    }

    private fun onCategorySelected(category: String) {
        requireFragmentManager()
                .beginTransaction()
                .replace(R.id.contentFrame, MainFragment.newInstance(category))
                .addToBackStack(null)
                .commit()
    }

    private fun buildCategories(): List<String> {
        val categories = listOf(getString(R.string.now_playing), getString(R.string.upcoming))
        val genres = GenreUtils.getGenres(requireContext()).toList().map { it.name }
        return categories + genres
    }

    private fun initSearchResultsRecyclerView() {
        results_list.setOnItemClickListener { _, _, position, _ ->
            showSimilarMovies(position)
        }

        results_list.setOnItemLongClickListener { _, _, position, _ ->
            displayRatingDialog(position)
            true
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onPause() {
        super.onPause()
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
    }

    override fun onReselected() {
        val current = requireFragmentManager().findFragmentById(R.id.contentFrame)
        if (current is SearchFragment) {
            toggleKeyboard(true)
        } else {
            requireFragmentManager().popBackStack()
        }
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        search_clear.visibility = if (s.toString().isEmpty()) View.INVISIBLE else View.VISIBLE
    }

    override fun afterTextChanged(s: Editable?) = Unit

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

    private fun displayRatingDialog(position: Int) {
        val result = results_list.adapter.getItem(position) as SearchResult
        val title = result.title

        val options = arrayListOf(
                getString(R.string.show_more_like_this),
                getString(R.string.show_less_like_this)
        ).toTypedArray()

        AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setItems(options) { _, which ->
                    if (which == 2) {
                        addToWatchlist(result)
                    } else {
                        val rating = getRating(which)
                        addRating(position, rating)
                    }
                }
                .setCancelable(true)
                .show()
    }

    private fun addRating(position: Int, rating: Int) {
        val message: String = if (rating == 1) {
            getString(R.string.will_more_like_this)
        } else {
            getString(R.string.will_less_like_this)
        }

        val result = results_list.adapter.getItem(position) as SearchResult
        //History.add(result, rating);

        Snackbar.make(results_list, message, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo) { /* TODO */ }
                .setActionTextColor(UiUtils.getSnackbarColor(requireContext()))
                .show()
    }

    private fun getRating(which: Int): Int {
        return if (which == 0) Constants.LIKE else Constants.DISLIKE
    }

    private fun addToWatchlist(searchResult: SearchResult) {
        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Adding movie to watchlist ...")
        progressDialog.show()

        requireActivity().supportLoaderManager
                .initLoader(1, null, object : LoaderManager.LoaderCallbacks<Array<Long>> {
            override fun onCreateLoader(id: Int, args: Bundle?): Loader<Array<Long>> {
                return DownloadRuntimeReleaseLoader(requireContext(), searchResult.id)
            }

            override fun onLoadFinished(loader: Loader<Array<Long>>, results: Array<Long>) {
                if (results[1] != null) {
                    val releaseDate = Date(results[1])
                    searchResult.releaseDate = releaseDate
                }

                if (results[0] != null) {
                    val runtime = results[0].toInt()
                    searchResult.runtime = runtime
                    progressDialog.dismiss()
                    UiUtils.showToast(requireContext(), R.string.added_to_watchlist)
                }
            }

            override fun onLoaderReset(loader: Loader<Array<Long>>) {}
        })
    }

    private fun showSimilarMovies(position: Int) {
        val result = results_list.adapter.getItem(position) as SearchResult
        val id = result.id
        val title = result.title

        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.putExtra(Constants.SINGLE_MOVIE, true)
        intent.putExtra(Constants.MOVIE_ID, id)
        intent.putExtra(Constants.MOVIE_TITLE, title)

        startActivity(intent)
        requireActivity().finish()
    }

    private fun clearSearchBarContent() {
        search_clear.alpha = DISABLED
        search_box.text.clear()
    }

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            val input = v.text.toString().trim()
            if (input.isEmpty()) {
                search_box.text.clear()
                search_box.requestFocus()
            } else {
                toggleKeyboard(false)
                downloadQueryResults(input)
            }

            return true
        }

        return false
    }

    private fun downloadQueryResults(query: String) {
        val loaderManager = requireActivity().supportLoaderManager
        loaderManager.destroyLoader(0)
        loaderManager.initLoader(0, null,
                object : LoaderManager.LoaderCallbacks<ArrayList<SearchResult>> {
                    override fun onCreateLoader(id: Int, args: Bundle?): Loader<ArrayList<SearchResult>> {
                        return SearchActivity.QueryTaskLoader(requireContext(), query)
                    }

                    override fun onLoadFinished(loader: Loader<ArrayList<SearchResult>>,
                                                data: ArrayList<SearchResult>) {
                        if (!data.isEmpty()) {
                            val adapter = SearchAdapter(requireContext(), data)
                            results_list.adapter = adapter
                            toggleViews(DISPLAY_LIST)
                        } else {
                            toggleViews(DISPLAY_EMPTY)
                        }
                    }

                    override fun onLoaderReset(loader: Loader<ArrayList<SearchResult>>) {}
                })
    }

    private fun toggleViews(state: Int) {
        results_list.visibility = View.GONE
        placeholder_container.visibility = View.GONE
        loading_container.visibility = View.GONE

        when (state) {
            DISPLAY_LIST -> results_list.visibility = View.VISIBLE
            DISPLAY_LOADING -> loading_container.visibility = View.VISIBLE
            DISPLAY_EMPTY -> placeholder_container.visibility = View.VISIBLE
        }
    }

    private fun toggleKeyboard(show: Boolean) {
        search_box.requestFocus()
        val inputMethodManager = requireContext().inputMethodManager

        if (show) {
            inputMethodManager.showSoftInput(search_box, InputMethodManager.SHOW_IMPLICIT)
        } else {
            inputMethodManager.hideSoftInputFromWindow(search_box.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        search_box.removeTextChangedListener(this)
        super.onDestroyView()
    }

    companion object {

        private const val KEY_SEARCH_INTENT = "KEY_SEARCH_INTENT"

        @JvmStatic
        fun newInstance(extra: String? = null) = SearchFragment().apply {
            arguments = Bundle().apply { putString(KEY_SEARCH_INTENT, extra) }
        }

    }

}
