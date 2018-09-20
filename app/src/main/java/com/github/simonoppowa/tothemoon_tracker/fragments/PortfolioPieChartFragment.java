package com.github.simonoppowa.tothemoon_tracker.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.simonoppowa.tothemoon_tracker.R;
import com.github.simonoppowa.tothemoon_tracker.models.Coin;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class PortfolioPieChartFragment extends Fragment {
    private static final String CURRENCY_KEY = "currencyKey";
    private static final String PORTFOLIO_TOTAL_KEY = "portfolioTotalKey";
    private static final String COIN_LIST_KEY = "coinListKey";

    private String mUsedCurrency;
    private double mTotalPortfolio;
    private ArrayList<Coin> mCoinList;

    @BindView(R.id.portfolio_pie_chart)
    PieChart mChart;

    public PortfolioPieChartFragment() {
        // Required empty public constructor
    }

    public static PortfolioPieChartFragment newInstance(String currency, double portfolioTotal, ArrayList<Coin> coins) {
        PortfolioPieChartFragment fragment = new PortfolioPieChartFragment();
        Bundle args = new Bundle();
        args.putString(CURRENCY_KEY, currency);
        args.putDouble(PORTFOLIO_TOTAL_KEY, portfolioTotal);
        args.putParcelableArrayList(COIN_LIST_KEY, coins);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUsedCurrency = getArguments().getString(CURRENCY_KEY);
            mTotalPortfolio = getArguments().getDouble(PORTFOLIO_TOTAL_KEY);
            mCoinList = getArguments().getParcelableArrayList(COIN_LIST_KEY);
        }  else {
        throw new NullPointerException("No bundle was passed to PortfolioPieChartFragment");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_portfolio_pie_chart, container, false);

        // Set up Libraries
        Timber.plant(new Timber.DebugTree());
        ButterKnife.bind(this, view);

        createPieChart();

        return view;
    }

    private void createPieChart() {

        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(5, 10, 5, 5);
        mChart.getLegend().setEnabled(false);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        //mChart.setCenterTextTypeface(mTfLight);
        //mChart.setCenterText(generateCenterSpannableText());

        mChart.setDrawHoleEnabled(true);
        mChart.setHoleColor(R.color.colorPrimary);

        mChart.setTransparentCircleColor(R.color.colorPrimary);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(58f);
        mChart.setTransparentCircleRadius(61f);

        mChart.setDrawCenterText(true);
        mChart.setCenterText("Portfolio");
        mChart.setCenterTextColor(R.color.defaultTextColor);

        mChart.setRotationAngle(0);
        // enable rotation of the chart by touch
        mChart.setRotationEnabled(true);
        mChart.setHighlightPerTapEnabled(false);

        // mChart.setUnit(" €");
        // mChart.setDrawUnitsInChart(true);

        // add a selection listener
        //mChart.setOnChartValueSelectedListener(this);

        setData();

        mChart.animateY(1400);
        // mChart.spin(2000, 0, 360);

        // entry label styling
        mChart.setEntryLabelColor(R.color.defaultTextColor);
        mChart.setEntryLabelTextSize(12f);

    }

    private void setData() {

        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

        for(Coin coin : mCoinList) {
            entries.add(new PieEntry((float) coin.getCurrentPrice(), coin.getFullName()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "Portfolio");

        dataSet.setDrawIcons(false);

        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(R.color.defaultTextColor);
        //data.setValueTypeface(mTfLight);
        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

}
