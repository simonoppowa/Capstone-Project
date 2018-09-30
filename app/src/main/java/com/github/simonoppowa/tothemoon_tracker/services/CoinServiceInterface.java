package com.github.simonoppowa.tothemoon_tracker.services;

import com.google.gson.JsonElement;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CoinServiceInterface {

    @GET("top/exchanges/full")
    Observable<JsonElement> getCoinInfo(@Query("fsym") String coinName, @Query("tsym") String currencyName, @Query("limit") int limit);

    @GET("histohour")
    Observable<Response<JsonElement>> get24hCoinChange(@Query("fsym") String coinName, @Query("tsym") String currencyName, @Query("limit") int limit);

    @GET("all/coinlist")
    Call<JsonElement> getFullCoinList();

}
