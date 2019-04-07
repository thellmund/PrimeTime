package com.hellmund.primetime.ui.main;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.hellmund.primetime.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.contentFrame, MainFragment.newInstance())
                    .commit();
        }
    }

}