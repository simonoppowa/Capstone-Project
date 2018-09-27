package com.github.simonoppowa.tothemoon_tracker.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.simonoppowa.tothemoon_tracker.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class RocketImageFragment extends Fragment {

    private static final String DAILY_CHANGE_PCT = "dailyChangePct";

    private double mDailyChangePct;

    @BindView(R.id.rocket_image_view)
    ImageView mRocketImageView;
    @BindView(R.id.moon_image_view)
    ImageView mMoonImageView;

    public RocketImageFragment() {
        // Required empty public constructor
    }

    public static RocketImageFragment newInstance(double dailyChangePct) {
        RocketImageFragment fragment = new RocketImageFragment();
        Bundle args = new Bundle();
        args.putDouble(DAILY_CHANGE_PCT, dailyChangePct);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDailyChangePct = getArguments().getDouble(DAILY_CHANGE_PCT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_rocket_image, container, false);

        // Set up Libraries
        Timber.plant(new Timber.DebugTree());
        ButterKnife.bind(this, view);

        mRocketImageView.setImageResource(R.drawable.ic_rocket);
        mMoonImageView.setImageResource(R.drawable.ic_moon);

        return view;
    }
}
