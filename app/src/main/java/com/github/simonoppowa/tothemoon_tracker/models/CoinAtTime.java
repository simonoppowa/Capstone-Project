package com.github.simonoppowa.tothemoon_tracker.models;

public class CoinAtTime extends Coin {

    private long atTime;

    public CoinAtTime(String name, double currentPrice) {
        super(name, currentPrice);
    }

    public long getAtTime() {
        return atTime;
    }

    public void setAtTime(long atTime) {
        this.atTime = atTime;
    }
}
