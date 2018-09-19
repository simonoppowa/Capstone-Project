package com.github.simonoppowa.tothemoon_tracker.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.simonoppowa.tothemoon_tracker.R;
import com.github.simonoppowa.tothemoon_tracker.models.Coin;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CoinAdapter extends RecyclerView.Adapter<CoinAdapter.ViewHolder> {

    private final Context context;
    private List<Coin> mCoins;

    public CoinAdapter(Context context, List<Coin> coins) {
        this.context = context;
        mCoins = coins;
    }

    public void setCoinList(List<Coin> coins) {
        mCoins = coins;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.coin_card_item, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.coinNameTextView.setText(mCoins.get(position).getName());
        holder.coinFullNameTextView.setText(mCoins.get(position).getFullName());
        holder.coin24hChangePctTextView.setText(String.valueOf(mCoins.get(position).getChange24hPct()));
        holder.coin24ChangeTextView.setText(String.valueOf(mCoins.get(position).getChange24h()));
    }

    @Override
    public int getItemCount() {
        return mCoins.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.coin_icon_image_view)
        public ImageView coinIconImageView;
        @BindView(R.id.coin_name_text_view)
        public TextView coinNameTextView;
        @BindView(R.id.coin_full_name_text_view)
        public TextView coinFullNameTextView;
        @BindView(R.id.coin_24hchange_pct_text_view)
        public TextView coin24hChangePctTextView;
        @BindView(R.id.coin_24change_text_view)
        public TextView coin24ChangeTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

        }
    }
}
