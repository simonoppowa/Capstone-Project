package com.github.simonoppowa.tothemoon_tracker.utils;

import com.github.simonoppowa.tothemoon_tracker.models.Coin;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class JsonUtils {

    public static Coin getCoinFromResponse(JsonElement responseJson) {

        JsonObject dataJsonObject = responseJson.getAsJsonObject().getAsJsonObject("Data");
        JsonElement coinInfoJsonObject = dataJsonObject.getAsJsonObject("CoinInfo");

        Gson gson = new Gson();

        Coin coin = gson.fromJson(coinInfoJsonObject, Coin.class);

        return coin;
    }
}
