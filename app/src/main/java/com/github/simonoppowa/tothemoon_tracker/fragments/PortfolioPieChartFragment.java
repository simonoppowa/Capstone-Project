package com.github.simonoppowa.tothemoon_tracker.fragments;


import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.simonoppowa.tothemoon_tracker.R;
import com.github.simonoppowa.tothemoon_tracker.activities.MainActivity;
import com.github.simonoppowa.tothemoon_tracker.models.Coin;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

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

    private List<Bitmap> coinBitmaps;
    private List<Palette> mPalettes;

    private int callsRemaining;

    // Picasso doesn't hold a strong reference to the Target object
    private List<Target> mTarget;

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

            mPalettes = new ArrayList<>();
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

        //setData();
        getColorsFromImages();



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

        // add a lot of chartColor

        ArrayList<Integer> chartColors = new ArrayList<>();
        ArrayList<Integer> textColors = new ArrayList<>();

        for(Palette palette : mPalettes) {
            Palette.Swatch chartSwatch = palette.getDominantSwatch();
            chartColors.add(chartSwatch.getRgb());
            textColors.add(chartSwatch.getBodyTextColor());
        }

        dataSet.setColors(chartColors);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        //data.setValueTextColor(R.color.defaultTextColor);
        data.setValueTextColors(textColors);
        //data.setValueTypeface(mTfLight);

        //TODO
        //mChart.setEntryLabelColor(R.color.defaultTextColor);

        mChart.setData(data);

        // undo all highlights
        mChart.highlightValues(null);

        mChart.invalidate();
    }

    private void getColorsFromImages() {
        callsRemaining = mCoinList.size();
        mTarget = new ArrayList<>();

        for(Coin coin : mCoinList) {
            String imageUrl = MainActivity.CRYPTOCOMPARE_BASE_URL + coin.getImageUrl() + "?width=50";
            Timber.d(imageUrl);

            Target newTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    Palette.from(bitmap)
                            .generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(@Nullable Palette palette) {
                                    synchronized (this) {
                                        mPalettes.add(palette);
                                        callsRemaining--;
                                        Timber.d("Calls remaining: %s", callsRemaining);
                                    }

                                    if(callsRemaining == 0) {
                                        setData();

                                        mChart.animateY(1400);

                                        // entry label styling
                                        mChart.setEntryLabelTextSize(12f);
                                    }
                                }
                            });
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    //TODO
                    Timber.d("Call failed");
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };

            mTarget.add(newTarget);

            Picasso.get()
                    .load(imageUrl)
                    .into(newTarget);
        }
    }

}
