package com.github.simonoppowa.tothemoon_tracker.fragments;

import android.content.Context;
import android.graphics.Shader;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.simonoppowa.tothemoon_tracker.R;
import com.github.simonoppowa.tothemoon_tracker.utils.ColorUtils;
import com.github.simonoppowa.tothemoon_tracker.utils.NumberFormatUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class PortfolioFragment extends Fragment {

    private static final String CURRENCY_KEY = "currencyKey";
    private static final String PORTFOLIO_KEY = "portfolioKey";
    private static final String PORTFOLIO_CHANGE_KEY = "portfolioChangeKey";
    private static final String PORTFOLIO_CHANGE_PCT_KEY = "portfolioChangePctKey";

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

    public static PortfolioFragment newInstance(String currency, double portfolioTotal, double portfolioChangeDaily,
                                                double portfolioChangeDailyPct) {
        PortfolioFragment fragment = new PortfolioFragment();
        Bundle args = new Bundle();
        args.putString(CURRENCY_KEY, currency);
        args.putDouble(PORTFOLIO_KEY, portfolioTotal);
        args.putDouble(PORTFOLIO_CHANGE_KEY, portfolioChangeDaily);
        args.putDouble(PORTFOLIO_CHANGE_PCT_KEY, portfolioChangeDailyPct);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            Bundle bundle = getArguments();

            mUsedCurrency = bundle.getString(CURRENCY_KEY);
            mPortfolioTotal = bundle.getDouble(PORTFOLIO_KEY);
            mPortfolioChangeDaily = bundle.getDouble(PORTFOLIO_CHANGE_KEY);
            mPortfolioChangeDailyPct = bundle.getDouble(PORTFOLIO_CHANGE_PCT_KEY);
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
        Shader textShaderTotal = ColorUtils.pickShaderFromChange(getContext(), mPortfolioChangeDailyPct, mTotalPortfolioTV);
        Shader textShaderElse = ColorUtils.pickShaderFromChange(getContext(), mPortfolioChangeDailyPct, mPortfolioChangeDailyPCTTV);

        mTotalPortfolioTV.getPaint().setShader(textShaderTotal);
        mPortfolioChangeDailyPCTTV.getPaint().setShader(textShaderElse);
        mPortfolioChangeDailyTV.getPaint().setShader(textShaderElse);

        mTotalPortfolioTV.setText(mUsedCurrency + " " + NumberFormatUtils.format2Decimal(mPortfolioTotal));
        mPortfolioChangeDailyPCTTV.setText(NumberFormatUtils.format2Decimal(mPortfolioChangeDailyPct) + "%");
        mPortfolioChangeDailyTV.setText(mUsedCurrency + " " + NumberFormatUtils.format2Decimal(mPortfolioChangeDaily));

        return view;
    }
}
