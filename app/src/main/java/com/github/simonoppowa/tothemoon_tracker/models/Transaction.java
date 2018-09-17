package com.github.simonoppowa.tothemoon_tracker.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "transaction_table")
public class Transaction {

    @PrimaryKey
    @NotNull
    @ColumnInfo(name = "coin_name")
    private String coinName;
    @ColumnInfo(name = "trade_pride_usd")
    private long tradePriceUSD;
    @ColumnInfo(name = "quantity")
    private double quantity;


    public Transaction(String coinName, long tradePriceUSD, double quantity) {
        this.coinName = coinName;
        this.tradePriceUSD = tradePriceUSD;
        this.quantity = quantity;
    }

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public long getTradePriceUSD() {
        return tradePriceUSD;
    }

    public void setTradePriceUSD(long tradePriceUSD) {
        this.tradePriceUSD = tradePriceUSD;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Transaction) {
            if(((Transaction) obj).coinName.equals(coinName))  {
                return true;
            }
        }
        return false;
    }
}
