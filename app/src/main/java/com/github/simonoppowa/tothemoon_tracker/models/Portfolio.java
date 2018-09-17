package com.github.simonoppowa.tothemoon_tracker.models;

public class Portfolio {
    private double totalPrice;
    private double change24h;
    private double change24hPct;

    public Portfolio(double totalPrice, double change24h, double change24hPct) {
        this.totalPrice = totalPrice;
        this.change24h = change24h;
        this.change24hPct = change24hPct;
    }

    public Portfolio() {
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getChange24h() {
        return change24h;
    }

    public void setChange24h(double change24h) {
        this.change24h = change24h;
    }

    public double getChange24hPct() {
        return change24hPct;
    }

    public void setChange24hPct(double change24hPct) {
        this.change24hPct = change24hPct;
    }
}
