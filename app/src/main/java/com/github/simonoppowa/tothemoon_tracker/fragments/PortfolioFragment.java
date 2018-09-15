package com.github.simonoppowa.tothemoon_tracker.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.simonoppowa.tothemoon_tracker.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class PortfolioFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";

    @BindView(R.id.total_portfolio_value)
    TextView mTotalPortfolioTV;
    @BindView(R.id.daily_portfolio_percentage)
    TextView mPortfolioChangeDailyPCTTV;
    @BindView(R.id.daily_portfolio_value)
    TextView mPortfolioChangeDailyTV;

    private String mUsedCurrency;
    private long mPortfolioTotal;
    private long mPortfolioChangeDaily;
    private int mPortfolioChangeDailyPct;

    public PortfolioFragment() {
        // Required empty public constructor
    }

    public static PortfolioFragment newInstance(String currency, long portfolioTotal, long portfolioChangeDaily, int portfolioChangeDailyPct) {
        PortfolioFragment fragment = new PortfolioFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, currency);
        args.putLong(ARG_PARAM2, portfolioTotal);
        args.putLong(ARG_PARAM3, portfolioChangeDaily);
        args.putInt(ARG_PARAM4, portfolioChangeDailyPct);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            Bundle bundle = getArguments();


            mUsedCurrency = bundle.getString(ARG_PARAM1);
            mPortfolioTotal = bundle.getLong(ARG_PARAM2);
            mPortfolioChangeDaily = bundle.getLong(ARG_PARAM3);
            mPortfolioChangeDailyPct = bundle.getInt(ARG_PARAM4);
        } else {
            throw new NullPointerException("No bundle was passed to PortfolioFragment");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);

        // Set up Libraries
        Timber.plant(new Timber.DebugTree());
        ButterKnife.bind(this, view);

        // Set textViews
        mTotalPortfolioTV.setText(String.valueOf(mPortfolioTotal));
        mPortfolioChangeDailyPCTTV.setText(String.valueOf(mPortfolioChangeDailyPct) + "%");
        mPortfolioChangeDailyTV.setText(String.valueOf(mPortfolioChangeDaily));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
}
