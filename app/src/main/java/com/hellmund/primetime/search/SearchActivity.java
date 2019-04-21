package com.hellmund.primetime.search;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hellmund.primetime.R;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contentFrame, SearchFragment.newInstance(null))
                    .commit();
        }
    }

}
