package com.github.simonoppowa.tothemoon_tracker.utils;

import android.text.TextUtils;

import com.github.simonoppowa.tothemoon_tracker.models.Coin;
import com.github.simonoppowa.tothemoon_tracker.models.CoinAtTime;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class JsonUtils {

    public static Coin getCoinFromResponse(JsonElement responseJson) {

        if(TextUtils.equals(responseJson.getAsJsonObject().get("Response").getAsString(), "Error")) {
            Timber.d("Response error");
            return null;
        }
        JsonObject dataJsonObject = responseJson.getAsJsonObject().getAsJsonObject("Data");

        JsonElement coinInfoJsonElement = dataJsonObject.getAsJsonObject("CoinInfo");
        JsonElement coinPriceDataJsonElement = dataJsonObject.getAsJsonObject("AggregatedData");

        String coinName = coinInfoJsonElement.getAsJsonObject().get("Name").getAsString();
        String fullName = coinInfoJsonElement.getAsJsonObject().get("FullName").getAsString();
        String imageUrl = coinInfoJsonElement.getAsJsonObject().get("ImageUrl").getAsString();

        double currentPrice = coinPriceDataJsonElement.getAsJsonObject().get("PRICE").getAsDouble();
        double change24hPct = coinPriceDataJsonElement.getAsJsonObject().get("CHANGEPCT24HOUR").getAsDouble();
        double change24h = coinPriceDataJsonElement.getAsJsonObject().get("CHANGE24HOUR").getAsDouble();

        return new Coin(coinName, fullName, imageUrl, currentPrice, change24hPct, change24h);
    }

    public static List<CoinAtTime> getCoinAtTimeListFromResponse(JsonElement responseJson, String coinName) {

        List<CoinAtTime> coinAtTimeList = new ArrayList<>();

        JsonArray dataJsonArray = responseJson.getAsJsonObject().getAsJsonArray("Data");

        for(JsonElement jsonElement : dataJsonArray) {
            long atTime;
            double price;

            atTime = jsonElement.getAsJsonObject().get("time").getAsLong();
            price = jsonElement.getAsJsonObject().get("close").getAsDouble();

            CoinAtTime newCoinAtTime = new CoinAtTime(coinName, price);
            newCoinAtTime.setAtTime(atTime);
            coinAtTimeList.add(newCoinAtTime);
        }

        return coinAtTimeList;
    }

    public static List<Coin> getCoinListFromResponse(JsonElement responseJson) {

        List<Coin> coinList = new ArrayList<>();

        JsonObject dataJsonArray = responseJson.getAsJsonObject().getAsJsonObject("Data");

        for (Map.Entry<String,JsonElement> entry : dataJsonArray.entrySet()) {

                String coinName = entry.getValue().getAsJsonObject().get("Name").getAsString();
                String fullName = entry.getValue().getAsJsonObject().get("CoinName").getAsString();
                String imageUrl = "";
                // Check if image available
                if(entry.getValue().getAsJsonObject().get("ImageUrl") != null) {
                    imageUrl = entry.getValue().getAsJsonObject().get("ImageUrl").getAsString();
                }

                coinList.add(new Coin(coinName, fullName, imageUrl, 0, 0, 0));
        }

        return coinList;
    }
}
