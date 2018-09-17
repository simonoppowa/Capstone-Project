package com.github.simonoppowa.tothemoon_tracker.utils;

import com.github.simonoppowa.tothemoon_tracker.models.Coin;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonUtils {

    public static Coin getCoinFromResponse(JsonElement responseJson) {

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
}
