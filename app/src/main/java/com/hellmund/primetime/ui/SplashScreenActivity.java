package com.hellmund.primetime.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.hellmund.primetime.R;
import com.hellmund.primetime.ui.introduction.IntroductionActivity;
import com.hellmund.primetime.ui.main.MainActivity;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (isFirstLaunch()) {
            openIntroduction();
        } else {
            openMain();
        }
    }

    private void openIntroduction() {
        Intent intent = new Intent(this, IntroductionActivity.class);
        startActivity(intent);
        finish();
    }

    private void openMain() {
        Intent intent = new Intent(this, MainActivity.class);

        if (getIntent().getExtras() != null) {
            final String shortcut = (String) getIntent().getExtras().get("intent");
            intent.putExtra("intent", shortcut);
        }

        startActivity(intent);
        finish();
    }

    private boolean isFirstLaunch() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPrefs.getBoolean("firstLaunchOfPrimeTime", true);
    }

}
