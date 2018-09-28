package com.github.simonoppowa.tothemoon_tracker.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import ir.mirrajabi.searchdialog.core.Searchable;

public class Coin implements Parcelable, Searchable {

    @SerializedName("Name")
    private String name;
    @SerializedName("FullName")
    private String fullName;
    @SerializedName("ImageUrl")
    private String imageUrl;
    private double currentPrice;
    private double change24hPct;
    private double change24h;

    public Coin(String name, String fullName, String imageUrl, double currentPrice, double change24hPct, double change24h) {
        this.name = name;
        this.fullName = fullName;
        this.imageUrl = imageUrl;
        this.currentPrice = currentPrice;
        this.change24hPct = change24hPct;
        this.change24h = change24h;
    }

    public Coin(String name, double currentPrice) {
        this.name = name;
        this.fullName = "";
        this.imageUrl = "";
        this.currentPrice = currentPrice;
        this.change24hPct = 0;
        this.change24h = 0;
    }

    /**
     * Parcelable constructor
     */
    private Coin(Parcel input) {
        name = input.readString();
        fullName = input.readString();
        imageUrl = input.readString();
        currentPrice = input.readDouble();
        change24hPct = input.readDouble();
        change24h = input.readDouble();

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public double getChange24hPct() {
        return change24hPct;
    }

    public void setChange24hPct(double change24hPct) {
        this.change24hPct = change24hPct;
    }

    public double getChange24h() {
        return change24h;
    }

    public void setChange24h(double change24h) {
        this.change24h = change24h;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String getTitle() {
        return fullName;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(fullName);
        parcel.writeString(imageUrl);
        parcel.writeDouble(currentPrice);
        parcel.writeDouble(change24hPct);
        parcel.writeDouble(change24h);
    }

    public static final Parcelable.Creator<Coin> CREATOR = new Parcelable.Creator<Coin>() {

        @Override
        public Coin createFromParcel(Parcel parcel) {
            return new Coin(parcel);
        }

        @Override
        public Coin[] newArray(int i) {
            return new Coin[i];
        }
    };

}
