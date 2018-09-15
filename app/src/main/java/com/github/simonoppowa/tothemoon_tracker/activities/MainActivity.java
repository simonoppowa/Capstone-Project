package com.github.simonoppowa.tothemoon_tracker.activities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.github.simonoppowa.tothemoon_tracker.R;
import com.github.simonoppowa.tothemoon_tracker.databases.TransactionDatabase;
import com.github.simonoppowa.tothemoon_tracker.models.Coin;
import com.github.simonoppowa.tothemoon_tracker.models.Transaction;
import com.github.simonoppowa.tothemoon_tracker.utils.CoinServiceInterface;
import com.github.simonoppowa.tothemoon_tracker.utils.JsonUtils;
import com.google.gson.JsonElement;

import java.util.List;

import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private static final String CRYPTOCOMPARE_BASE_URL = "https://min-api.cryptocompare.com/data/";
    private static final String CRYPTOCOMPARE_IMAGE_BASE_URL = "https://www.cryptocompare.com/media/";

    public static final String CURRENCY_SP_KEY = "currencySharedPreference";
    public static final String DEFAULT_CURRENCY = "USD";

    private static Retrofit retrofit;

    private TransactionDatabase transactionDatabase;

    private String mUsedCurrency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up libraries
        Timber.plant(new Timber.DebugTree());
        ButterKnife.bind(this);

        setupSharedPreferences();

        transactionDatabase = TransactionDatabase.getDatabase(getApplicationContext());

        // Retrofit API call
        retrofit = new Retrofit.Builder()
                .baseUrl(CRYPTOCOMPARE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();


        CoinServiceInterface coinServiceInterface = retrofit.create(CoinServiceInterface.class);

        Call<JsonElement> call = coinServiceInterface.getCoinInfo("BTC", mUsedCurrency);

        Timber.d(String.valueOf(call.request().url()));

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                //TODO
                if(response.code() == 200) {
                    JsonElement responseJSON = response.body();
                    Timber.d(responseJSON.toString());

                    Coin newCoin = JsonUtils.getCoinFromResponse(responseJSON);

                    Timber.d("Fetched coin: " + newCoin.getFullName() + ", " + newCoin.getName() + ", " + newCoin.getImageUrl());
                } else {
                    Timber.d("API Call returned Error: %s", String.valueOf(response.body()));
                }

            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Timber.d("Coin info call failed");
            }
        });


        final Call<JsonElement> priceCall = coinServiceInterface.getSingleCoinPrice("BTC", mUsedCurrency);
        priceCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                //TODO

                if(response.code() == 200) {
                    JsonElement jsonElement = response.body();
                    Timber.d(jsonElement.getAsJsonObject().get("USD").toString());
                } else {
                    Timber.d("API Call returned Error: %s", String.valueOf(response.body()));
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Timber.d("Price call failed");
            }
        });

        // Room DB
        new DatabaseAsyncTask().execute();

    }

    @SuppressLint("ApplySharedPref")
    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences.contains(CURRENCY_SP_KEY)) {
            mUsedCurrency = sharedPreferences.getString(CURRENCY_SP_KEY, DEFAULT_CURRENCY);
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(CURRENCY_SP_KEY, DEFAULT_CURRENCY);
            editor.commit();
            mUsedCurrency = DEFAULT_CURRENCY;
        }
    }

    //TODO
    private class DatabaseAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            Transaction transaction = new Transaction("BTC", 1, 1.1);

            transactionDatabase.transactionDao().insertTransaction(transaction);

            List<Transaction> transactionList = transactionDatabase.transactionDao().getAllTransactions();

            for(Transaction t : transactionList) {
                Timber.d(t.getCoinName());
            }


            return null;
        }
    }
}
