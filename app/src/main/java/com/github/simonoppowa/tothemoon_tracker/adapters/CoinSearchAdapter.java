package com.github.simonoppowa.tothemoon_tracker.adapters;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.simonoppowa.tothemoon_tracker.R;
import com.github.simonoppowa.tothemoon_tracker.models.Coin;
import com.github.simonoppowa.tothemoon_tracker.utils.PicassoUtils;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import ir.mirrajabi.searchdialog.StringsHelper;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;
import ir.mirrajabi.searchdialog.core.Searchable;

public class CoinSearchAdapter<T extends Searchable> extends RecyclerView.Adapter<CoinSearchAdapter.ViewHolder> {

    private Context mContext;
    private List<T> mItems = new ArrayList<>();
    private LayoutInflater mLayoutInflater;
    private int mLayout;
    private SearchResultListener mSearchResultListener;
    private AdapterViewBinder<T> mViewBinder;
    private String mSearchTag;
    private boolean mHighlightPartsInCommon = true;
    private int mHighlightColor;
    private BaseSearchDialogCompat mSearchDialog;

    public CoinSearchAdapter(Context context, @LayoutRes int layout, List<T> items) {
        this(context, layout, null, items);
    }

    public CoinSearchAdapter(Context context, @LayoutRes int layout, @Nullable AdapterViewBinder<T> viewBinder, List<T> items) {
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        this.mItems = items;
        this.mLayout = layout;
        this.mViewBinder = viewBinder;
        this.mHighlightColor = context.getResources().getColor(R.color.highlightTextColor);
    }

    public List<T> getItems() {
        return mItems;
    }

    public void setItems(List<T> objects) {
        this.mItems = objects;
        notifyDataSetChanged();
    }

    public T getItem(int position) {
        return mItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public CoinSearchAdapter<T> setViewBinder(AdapterViewBinder<T> viewBinder) {
        this.mViewBinder = viewBinder;
        return this;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = mLayoutInflater.inflate(mLayout, parent, false);

        convertView.setTag(new ViewHolder(convertView));

        return (ViewHolder) convertView.getTag();
    }

    @Override
    public void onBindViewHolder(CoinSearchAdapter.ViewHolder holder, int position) {
        initializeViews(getItem(position), holder, position);
    }

    private void initializeViews(final T object, final CoinSearchAdapter.ViewHolder holder, final int position) {
        if (mViewBinder != null) {
            mViewBinder.bind(holder, object, position);
        }

        Coin coin = (Coin) object;

        RoundedImageView image = holder.getViewById(R.id.search_coin_image_view);
        TextView fullNameTextView = holder.getViewById(R.id.search_coin_full_name_text_view);
        TextView nameTextView = holder.getViewById(R.id.search_coin_name_text_view);

        Picasso.get()
                .load(PicassoUtils.getFullCoinImageUrl(coin.getImageUrl()))
                .into(image);

        nameTextView.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
        fullNameTextView.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));

        if (mSearchTag != null && mHighlightPartsInCommon) {
            nameTextView.setText(StringsHelper.highlightLCS(coin.getTitle(), getSearchTag(), mHighlightColor));
            fullNameTextView.setText(StringsHelper.highlightLCS(coin.getName(), getSearchTag(), mHighlightColor));
        } else {
            nameTextView.setText(object.getTitle());
            fullNameTextView.setText(coin.getName());
        }

        if (mSearchResultListener != null) {
            holder.getBaseView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mSearchResultListener.onSelected(mSearchDialog, object, position);
                }
            });
        }
    }

    public SearchResultListener getSearchResultListener() {
        return mSearchResultListener;
    }

    public void setSearchResultListener(SearchResultListener searchResultListener) {
        this.mSearchResultListener = searchResultListener;
    }

    public String getSearchTag() {
        return mSearchTag;
    }

    public CoinSearchAdapter<T> setSearchTag(String searchTag) {
        mSearchTag = searchTag;
        return this;
    }

    public boolean isHighlightPartsInCommon() {
        return mHighlightPartsInCommon;
    }

    public CoinSearchAdapter<T> setHighlightPartsInCommon(boolean highlightPartsInCommon) {
        mHighlightPartsInCommon = highlightPartsInCommon;
        return this;
    }

    public CoinSearchAdapter<T> setHighlightColor(int highlightColor) {
        mHighlightColor = highlightColor;
        return this;
    }

    public void setSearchDialog(BaseSearchDialogCompat searchDialog) {
        mSearchDialog = searchDialog;
    }

    public interface AdapterViewBinder<T> {
        void bind(ViewHolder holder, T item, int position);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private View mBaseView;

        public ViewHolder(View view) {
            super(view);
            mBaseView = view;
        }

        public View getBaseView() {
            return mBaseView;
        }

        public <T> T getViewById(@IdRes int id) {
            return (T) mBaseView.findViewById(id);
        }

        public void clearAnimation(@IdRes int id) {
            mBaseView.findViewById(id).clearAnimation();
        }
    }
}
