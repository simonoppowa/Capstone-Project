package com.github.simonoppowa.tothemoon_tracker.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.simonoppowa.tothemoon_tracker.R;
import com.github.simonoppowa.tothemoon_tracker.adapters.CoinAdapter;
import com.github.simonoppowa.tothemoon_tracker.models.Coin;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


public class CoinsInfoFragment extends Fragment {

    private static final String CURRENCY_KEY = "param1";
    private static final String COIN_LIST_KEY = "param2";

    @BindView(R.id.coin_card_recycler_view)
    RecyclerView mCoinInfoRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private CoinAdapter mCoinAdapter;

    private String mUsedCurrency;
    private List<Coin> mCoins;


    public CoinsInfoFragment() {
        // Required empty public constructor
    }


    public static CoinsInfoFragment newInstance(String currency, ArrayList<Coin> coins) {
        CoinsInfoFragment fragment = new CoinsInfoFragment();
        Bundle args = new Bundle();
        args.putString(CURRENCY_KEY, currency);
        args.putParcelableArrayList(COIN_LIST_KEY, coins);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUsedCurrency = getArguments().getString(CURRENCY_KEY);
            mCoins = getArguments().getParcelableArrayList(COIN_LIST_KEY);
        }



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coins_info, container, false);

        // Set up Libraries
        Timber.plant(new Timber.DebugTree());
        ButterKnife.bind(this, view);


        // Create RecyclerView
        mLinearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        mCoinAdapter = new CoinAdapter(getContext(), mCoins);

        mCoinInfoRecyclerView.setLayoutManager(mLinearLayoutManager);
        mCoinInfoRecyclerView.setAdapter(mCoinAdapter);


        // Inflate the layout for this fragment
        return view;
    }

}
