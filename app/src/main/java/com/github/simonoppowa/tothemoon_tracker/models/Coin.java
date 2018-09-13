package com.github.simonoppowa.tothemoon_tracker.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Coin implements Parcelable {

    private String name;
    private String fullName;
    private String imageUrl;

    public Coin(String name, String fullName, String imageUrl) {
        this.name = name;
        this.fullName = fullName;
        this.imageUrl = imageUrl;
    }

    /**
     * Parcelable constructor
     */
    private Coin(Parcel input) {
        name = input.readString();
        fullName = input.readString();
        imageUrl = input.readString();
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


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(fullName);
        parcel.writeString(imageUrl);
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
