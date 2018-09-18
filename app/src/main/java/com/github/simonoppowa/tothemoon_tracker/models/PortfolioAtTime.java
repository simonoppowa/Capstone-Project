package com.github.simonoppowa.tothemoon_tracker.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class PortfolioAtTime extends Portfolio implements Parcelable{

    private long atTime;

    public PortfolioAtTime(double totalPrice, double change24h, double change24hPct, long atTime) {
        super(totalPrice, change24h, change24hPct);
        this.atTime = atTime;
    }

    private PortfolioAtTime(Parcel input) {
        setTotalPrice(input.readDouble());
        setAtTime(input.readLong());
    }

    public long getAtTime() {
        return atTime;
    }

    public Date getTimeAsDate() {
        return new Date(atTime);
    }

    public void setAtTime(long atTime) {
        this.atTime = atTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeDouble(getTotalPrice());
        parcel.writeLong(atTime);
    }

    public static final Parcelable.Creator<PortfolioAtTime> CREATOR = new Parcelable.Creator<PortfolioAtTime>() {


        @Override
        public PortfolioAtTime createFromParcel(Parcel parcel) {
            return new PortfolioAtTime(parcel);
        }

        @Override
        public PortfolioAtTime[] newArray(int i) {
            return new PortfolioAtTime[i];
        }
    };
}
