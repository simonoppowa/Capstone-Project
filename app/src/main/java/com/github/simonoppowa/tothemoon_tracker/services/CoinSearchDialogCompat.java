package com.github.simonoppowa.tothemoon_tracker.services;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.simonoppowa.tothemoon_tracker.R;
import com.github.simonoppowa.tothemoon_tracker.adapters.CoinSearchAdapter;
import com.github.simonoppowa.tothemoon_tracker.models.Coin;

import java.util.ArrayList;

import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat;
import ir.mirrajabi.searchdialog.SimpleSearchFilter;
import ir.mirrajabi.searchdialog.adapters.SearchDialogAdapter;
import ir.mirrajabi.searchdialog.core.BaseFilter;
import ir.mirrajabi.searchdialog.core.FilterResultListener;
import ir.mirrajabi.searchdialog.core.OnPerformFilterListener;
import ir.mirrajabi.searchdialog.core.SearchResultListener;
import ir.mirrajabi.searchdialog.core.Searchable;

public class CoinSearchDialogCompat<T extends Searchable> extends SimpleSearchDialogCompat<T> {

    String mTitle;
    String mSearchHint;
    private SearchResultListener<T> mSearchResultListener;

    private TextView mTxtTitle;
    private EditText mSearchBox;
    private RecyclerView mRecyclerView;
    private ProgressBar mProgressBar;
    private Handler mHandler;

    public CoinSearchDialogCompat(Context context, String title, String searchHint,
                                  @Nullable  Filter filter, ArrayList<T> items, SearchResultListener<T> searchResultListener) {
        super(context,title, searchHint, filter, items, searchResultListener);
        init(title, searchHint, searchResultListener);
    }

    private void init(String title, String searchHint,SearchResultListener<T> searchResultListener) {
        mTitle = title;
        mSearchHint = searchHint;
        mSearchResultListener = searchResultListener;
        setFilterResultListener(new FilterResultListener<T>() {
            @Override
            public void onFilter(ArrayList<T> items) {
                ((SearchDialogAdapter) getAdapter())
                        .setSearchTag(mSearchBox.getText().toString())
                        .setItems(items);
            }
        });
        mHandler = new Handler();
    }

    @Override
    protected void getView(View view) {
        setContentView(view);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setCancelable(true);
        mTxtTitle =  view.findViewById(ir.mirrajabi.searchdialog.R.id.txt_title);
        mSearchBox = view.findViewById(getSearchBoxId());
        mRecyclerView = view.findViewById(getRecyclerViewId());
        mProgressBar = view.findViewById(ir.mirrajabi.searchdialog.R.id.progress);

        mProgressBar.setVisibility(View.VISIBLE);
        setLoading(true);

        mTxtTitle.setText(mTitle);
        mTxtTitle.setTextColor(view.getContext().getResources().getColor(R.color.colorPrimary));
        mSearchBox.setHint(mSearchHint);
        mProgressBar.setIndeterminate(true);

        view.findViewById(ir.mirrajabi.searchdialog.R.id.dummy_background)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dismiss();
                    }
                });

        final CoinSearchAdapter adapter = new CoinSearchAdapter(getContext(), R.layout.search_item_adapter, getItems());
        adapter.setSearchResultListener(mSearchResultListener);
        adapter.setSearchDialog(this);

        FilterResultListener<Coin> filterResultListener = new FilterResultListener<Coin>() {
            @Override
            public void onFilter(ArrayList<Coin> coins) {
                ((CoinSearchAdapter) getAdapter())
                        .setSearchTag(mSearchBox.getText().toString())
                        .setItems(coins);
            }
        };
        setFilterResultListener((FilterResultListener<T>) filterResultListener);

        setAdapter(adapter);
        mSearchBox.requestFocus();

        SimpleSearchFilter<Coin> simpleSearchFilter = new SimpleSearchFilter<Coin>(adapter.getItems(), filterResultListener);
        setFilter(simpleSearchFilter);

        ((BaseFilter<T>) getFilter()).setOnPerformFilterListener(new OnPerformFilterListener() {
            @Override
            public void doBeforeFiltering() {
                setLoading(true);
            }

            @Override
            public void doAfterFiltering() {
                setLoading(false);
            }
        });
    }

    @Override
    public void setLoading(final boolean isLoading) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mProgressBar != null) {
                    mRecyclerView.setVisibility(!isLoading ? View.VISIBLE : View.GONE);
                }
                if (mRecyclerView != null) {
                    mProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                }
            }
        });
    }

    public CoinSearchDialogCompat setTitle(String title) {
        mTitle = title;
        return this;
    }

    public CoinSearchDialogCompat setSearchHint(String searchHint) {
        mSearchHint = searchHint;
        return this;
    }

    public CoinSearchDialogCompat setSearchResultListener(SearchResultListener<T> searchResultListener) {
        mSearchResultListener = searchResultListener;
        return this;
    }

    @LayoutRes
    @Override
    protected int getLayoutResId() {
        return ir.mirrajabi.searchdialog.R.layout.search_dialog_compat;
    }

    @LayoutRes
    @Override
    protected int getSearchBoxId() {
        return ir.mirrajabi.searchdialog.R.id.txt_search;
    }

    @LayoutRes
    @Override
    protected int getRecyclerViewId() {
        return ir.mirrajabi.searchdialog.R.id.rv_items;
    }
}
