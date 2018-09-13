package com.github.simonoppowa.tothemoon_tracker.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.github.simonoppowa.tothemoon_tracker.R;

import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up libraries
        Timber.plant(new Timber.DebugTree());
        ButterKnife.bind(this);
    }
}
