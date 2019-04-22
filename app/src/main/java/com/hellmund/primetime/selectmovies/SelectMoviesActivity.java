package com.hellmund.primetime.selectmovies;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.hellmund.primetime.R;
import com.hellmund.primetime.api.ApiClient;
import com.hellmund.primetime.database.PrimeTimeDatabase;
import com.hellmund.primetime.history.HistoryRepository;
import com.hellmund.primetime.main.MainActivity;
import com.hellmund.primetime.model2.Sample;
import com.hellmund.primetime.search.EqualSpacingGridItemDecoration;
import com.hellmund.primetime.selectgenres.GenresRepository;
import com.hellmund.primetime.utils.Constants;
import com.hellmund.primetime.utils.NetworkUtils;
import com.hellmund.primetime.utils.OnboardingHelper;
import com.hellmund.primetime.utils.UiUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kotlin.Unit;

public class SelectMoviesActivity extends AppCompatActivity
        implements LegacySamplesAdapter.OnInteractionListener {

    private final static int MIN_COUNT = 4;

    // TODO Remove
    private List<Sample> mSamples;
    private HashSet<Integer> mSelected;

    private SharedPreferences mSharedPrefs;

    @BindView(R.id.gridView)
    RecyclerView recyclerView;

    @BindView(R.id.button)
    AppCompatButton mSaveButton;

    @BindView(R.id.error_container)
    LinearLayout mErrorContainer;

    private ProgressDialog mProgressDialog;
    private SamplesAdapter adapter;

    private OnboardingHelper onboardingHelper;

    private SelectMoviesViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_movie);

        ButterKnife.bind(this);
        setupRecyclerView();

        onboardingHelper = new OnboardingHelper(this);

        mSaveButton.setOnClickListener(v -> saveMovies());

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        GenresRepository genresRepo = new GenresRepository(ApiClient.getInstance(), PrimeTimeDatabase.getInstance(this));
        HistoryRepository historyRepository = new HistoryRepository(PrimeTimeDatabase.getInstance(this));

        SelectMoviesRepository repository = new SelectMoviesRepository(ApiClient.getInstance(), historyRepository);
        SelectMoviesViewModel.Factory factory =
                new SelectMoviesViewModel.Factory(repository, genresRepo);

        viewModel = ViewModelProviders.of(this, factory).get(SelectMoviesViewModel.class);
        viewModel.getViewState().observe(this, this::render);
    }

    private void setupRecyclerView() {
        adapter = new SamplesAdapter(sample -> {
            viewModel.onItemClick(sample);
            return Unit.INSTANCE;
        });
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        final int spacing = Math.round(getResources().getDimension(R.dimen.default_space));
        recyclerView.addItemDecoration(new EqualSpacingGridItemDecoration(spacing, 3));
    }

    private void render(SelectMoviesViewState viewState) {
        mSamples = viewState.getData();

        if (viewState.isLoading()) {
            createProgressDialog();
            mProgressDialog.show();
        } else {
            mProgressDialog.dismiss();
        }

        if (viewState.isError()) {
            recyclerView.setVisibility(View.GONE);
            mErrorContainer.setVisibility(View.VISIBLE);
            mSaveButton.setVisibility(View.GONE);
        } else {
            /*SamplesAdapter adapter = new SamplesAdapter(this, mSamples);
            mGridView.setAdapter(adapter);
            adapter.notifyDataSetChanged();*/
            adapter.update(mSamples);

            recyclerView.setVisibility(View.VISIBLE);
            mErrorContainer.setVisibility(View.GONE);
            mSaveButton.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.error_button)
    public void tryDownloadAgain(View view) {
        viewModel.refresh();
    }

    @Override
    public void onItemSelected(View view, int position) {
        toggleItemState(view, position);
        setSaveButtonState();
    }

    private void toggleItemState(View view, int position) {
        if (mSelected == null) {
            mSelected = new HashSet<>();
        }

        final int id = mSamples.get(position).getId();

        if (mSelected.contains(id)) {
            view.setAlpha(Constants.DISABLED);
            mSelected.remove(id);
        } else {
            view.setAlpha(Constants.ENABLED);
            mSelected.add(id);
        }
    }

    private void setSaveButtonState() {
        final boolean enabled = mSelected.size() >= MIN_COUNT;
        mSaveButton.setClickable(enabled);
        mSaveButton.setEnabled(enabled);
    }

    private void createProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.downloading_samples));
        mProgressDialog.setCancelable(false);
    }

    private void saveMovies() {
        if (!mSaveButton.isEnabled()) {
            UiUtils.showToast(this, getString(R.string.select_at_least, MIN_COUNT));
        }

        if (NetworkUtils.isConnected(this)) {
            saveSelection();
            markIntroDone();
            openRecommendations();
        } else {
            UiUtils.showToast(this, getString(R.string.not_connected));
        }
    }

    private void markIntroDone() {
        onboardingHelper.setFirstLaunch(false);
    }

    private void openRecommendations() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void saveSelection() {
        List<Sample> samples = adapter.getItems();
        List<Sample> selected = new ArrayList<>();

        for (Sample sample : samples) {
            if (sample.getSelected()) {
                selected.add(sample);
            }
        }

        viewModel.store(selected);
    }

}
