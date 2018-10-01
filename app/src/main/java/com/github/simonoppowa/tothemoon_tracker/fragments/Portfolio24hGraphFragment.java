package com.github.simonoppowa.tothemoon_tracker.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.simonoppowa.tothemoon_tracker.R;
import com.github.simonoppowa.tothemoon_tracker.models.PortfolioAtTime;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class Portfolio24hGraphFragment extends Fragment {

    private static final String CURRENCY_KEY = "currencyKey";
    private static final String PORTFOLIO_AT_TIME_LIST_KEY = "portfolioAtTimeList";

    private String mUsedCurrency;
    private List<PortfolioAtTime> mPortfoliosAtTime;

    @BindView(R.id.portfolio_graph)
    LineChart mChart;

    public Portfolio24hGraphFragment() {
        // Required empty public constructor
    }

    public static Portfolio24hGraphFragment newInstance(String currency, List<PortfolioAtTime> portfoliosAtTime) {
        Portfolio24hGraphFragment portfolio24hGraphFragment = new Portfolio24hGraphFragment();
        Bundle args = new Bundle();
        args.putString(CURRENCY_KEY, currency);
        args.putParcelableArrayList(PORTFOLIO_AT_TIME_LIST_KEY, (ArrayList<? extends Parcelable>) portfoliosAtTime);
        portfolio24hGraphFragment.setArguments(args);

        return portfolio24hGraphFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            Bundle bundle = getArguments();

            mUsedCurrency = bundle.getString(CURRENCY_KEY);
            mPortfoliosAtTime =  bundle.getParcelableArrayList(PORTFOLIO_AT_TIME_LIST_KEY);
        } else {
            throw new NullPointerException("No bundle was passed to Portfolio24hGraphFragment");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_portfolio24h_graph, container, false);

        // Set up Libraries
        Timber.plant(new Timber.DebugTree());
        ButterKnife.bind(this, view);

        createGraph();

        return view;
    }

    private void createGraph() {

        mChart.setDrawGridBackground(true);
        mChart.getAxisLeft().setDrawGridLines(false);
        mChart.getXAxis().setDrawGridLines(false);
        mChart.getAxisRight().setEnabled(false);
        mChart.getXAxis().setDrawAxisLine(false);
        mChart.getAxisLeft().setEnabled(false);

        mChart.setAutoScaleMinMaxEnabled(true);

        mChart.getDescription().setEnabled(true);
        mChart.getDescription().setText(getString(R.string.graph_total_portfolio));
        mChart.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mChart.setGridBackgroundColor(getResources().getColor(R.color.colorPrimary));
        mChart.setDrawBorders(false);

        mChart.setPinchZoom(false);
        mChart.setDoubleTapToZoomEnabled(false);

        mChart.getLegend().setEnabled(false);
        mChart.setFocusable(false);
        mChart.setClickable(false);
        mChart.setHighlightPerTapEnabled(false);
        mChart.setHighlightPerDragEnabled(false);

        setGraphData();

        mChart.animateX(2500);

    }

    private void setGraphData() {

        List<Entry> values = createEntityList();

        LineDataSet set1;

        if (mChart.getData() != null &&
                mChart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet)mChart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            mChart.getData().notifyDataChanged();
            mChart.notifyDataSetChanged();
        } else {
            // create a dataset and give it a type
            set1 = new LineDataSet(values, getString(R.string.portfolio_name));

            set1.setDrawIcons(false);

            set1.setColor(Color.WHITE);
            set1.setCircleColor(Color.WHITE);
            set1.setValueTextColor(Color.WHITE);
            set1.setLineWidth(1f);
            set1.setCircleRadius(2f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(9f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormSize(15.f);

            set1.setFillColor(Color.WHITE);

            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the datasets

            // create a data object with the datasets
            LineData data = new LineData(dataSets);

            // set data
            mChart.setData(data);
        }
    }

    private List<Entry> createEntityList() {
        ArrayList<Entry> values = new ArrayList<>();

        int i = 0;
        for (PortfolioAtTime portfolioAtTime : mPortfoliosAtTime) {
            values.add( new Entry(i, (float) portfolioAtTime.getTotalPrice()));
            i++;
        }

        return values;
    }
}
