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
import com.github.simonoppowa.tothemoon_tracker.models.Transaction;
import com.github.simonoppowa.tothemoon_tracker.utils.NumberFormatUtils;
import com.github.simonoppowa.tothemoon_tracker.utils.PicassoUtils;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CoinAdapter extends RecyclerView.Adapter<CoinAdapter.ViewHolder> {

    private List<Coin> mCoins;
    private List<Transaction> mTransactions;
    private String mUsedCurrency;

    public CoinAdapter(List<Coin> coins, List<Transaction> transactions, String currency) {
        mCoins = coins;
        mTransactions = transactions;
        mUsedCurrency = currency;
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

        Coin selectedCoin = mCoins.get(position);
        Transaction selectedTransaction = mTransactions.get(mTransactions.indexOf(new Transaction(selectedCoin.getName(), 0, 0)));
        BigDecimal fullValueChange24h = new BigDecimal(selectedCoin.getChange24h()* selectedTransaction.getQuantity());

        Picasso.get()
                .load(PicassoUtils.getFullCoinImageUrl(selectedCoin.getImageUrl()))
                .into(holder.coinIconImageView);

        holder.coinNameTextView.setText(selectedCoin.getName());
        holder.coinFullNameTextView.setText(selectedCoin.getFullName());
        holder.coin24hChangePctTextView.setText(NumberFormatUtils.format2Decimal(selectedCoin.getChange24hPct()) + "%");
        holder.coin24ChangeTextView.setText(mUsedCurrency + NumberFormatUtils.format2Decimal(fullValueChange24h.doubleValue()));
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
