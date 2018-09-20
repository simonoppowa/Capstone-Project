package com.github.simonoppowa.tothemoon_tracker.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.simonoppowa.tothemoon_tracker.R;
import com.github.simonoppowa.tothemoon_tracker.utils.NumberFormatUtils;

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
    private double mPortfolioTotal;
    private double mPortfolioChangeDaily;
    private double mPortfolioChangeDailyPct;

    public PortfolioFragment() {
        // Required empty public constructor
    }

    public static PortfolioFragment newInstance(String currency, double portfolioTotal, double portfolioChangeDaily, double portfolioChangeDailyPct) {
        PortfolioFragment fragment = new PortfolioFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, currency);
        args.putDouble(ARG_PARAM2, portfolioTotal);
        args.putDouble(ARG_PARAM3, portfolioChangeDaily);
        args.putDouble(ARG_PARAM4, portfolioChangeDailyPct);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            Bundle bundle = getArguments();

            mUsedCurrency = bundle.getString(ARG_PARAM1);
            mPortfolioTotal = bundle.getDouble(ARG_PARAM2);
            mPortfolioChangeDaily = bundle.getDouble(ARG_PARAM3);
            mPortfolioChangeDailyPct = bundle.getDouble(ARG_PARAM4);
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
        mTotalPortfolioTV.setText(mUsedCurrency + " " + NumberFormatUtils.format2Decimal(mPortfolioTotal));
        mPortfolioChangeDailyPCTTV.setText(NumberFormatUtils.format2Decimal(mPortfolioChangeDailyPct) + "%");
        mPortfolioChangeDailyTV.setText(mUsedCurrency + NumberFormatUtils.format2Decimal(mPortfolioChangeDaily));

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }
}
