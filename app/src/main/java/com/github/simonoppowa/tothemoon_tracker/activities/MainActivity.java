package com.github.simonoppowa.tothemoon_tracker.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.simonoppowa.tothemoon_tracker.R;
import com.github.simonoppowa.tothemoon_tracker.models.Coin;
import com.github.simonoppowa.tothemoon_tracker.utils.CoinServiceInterface;
import com.github.simonoppowa.tothemoon_tracker.utils.JsonUtils;
import com.google.gson.JsonElement;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final String CRYPTOCOMPARE_BASE_URL = "https://min-api.cryptocompare.com/data/";

    private static Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up libraries
        Timber.plant(new Timber.DebugTree());
        ButterKnife.bind(this);

        //
        retrofit = new Retrofit.Builder()
                .baseUrl(CRYPTOCOMPARE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        CoinServiceInterface coinServiceInterface = retrofit.create(CoinServiceInterface.class);

        Call<JsonElement> call = coinServiceInterface.getCoinInfo("BTC", "USD");

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                //TODO
                JsonElement responseJSON = response.body();
                Timber.d(responseJSON.toString());

                Coin newCoin = JsonUtils.getCoinFromResponse(responseJSON);

                Timber.d("Fetched coin: " + newCoin.getFullName() + ", " + newCoin.getName() + ", " + newCoin.getImageUrl());

            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Timber.d("Coin info call failed");
            }
        });


        final Call<JsonElement> priceCall = coinServiceInterface.getSingleCoinPrice("BTC", "USD");
        priceCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                //TODO
                JsonElement jsonElement = response.body();
                Timber.d(jsonElement.getAsJsonObject().get("USD").toString());
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Timber.d("Price call failed");
            }
        });


    }
}
