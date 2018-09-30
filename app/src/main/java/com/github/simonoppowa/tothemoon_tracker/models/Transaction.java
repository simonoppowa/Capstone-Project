package com.github.simonoppowa.tothemoon_tracker.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

@Entity(tableName = "transaction_table")
public class Transaction implements Parcelable{

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

    public Transaction(String coinName, BigDecimal tradePriceUSD, BigDecimal quantity) {
        this.coinName = coinName;
        this.tradePriceUSD = tradePriceUSD.longValue();
        this.quantity = quantity.longValue();
    }

    /**
     * Parcelable constructor
     */
    private Transaction(Parcel input) {
        coinName = input.readString();
        tradePriceUSD = input.readLong();
        quantity = input.readDouble();
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
        return obj instanceof Transaction && ((Transaction) obj).coinName.equals(coinName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(coinName);
        parcel.writeLong(tradePriceUSD);
        parcel.writeDouble(quantity);
    }

    public static final Parcelable.Creator<Transaction> CREATOR = new Parcelable.Creator<Transaction>() {

        @Override
        public Transaction createFromParcel(Parcel parcel) {
            return new Transaction(parcel);
        }

        @Override
        public Transaction[] newArray(int i) {
            return new Transaction[i];
        }
    };
}
