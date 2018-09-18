package com.github.simonoppowa.tothemoon_tracker.activities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.github.simonoppowa.tothemoon_tracker.R;
import com.github.simonoppowa.tothemoon_tracker.databases.TransactionDatabase;
import com.github.simonoppowa.tothemoon_tracker.fragments.Portfolio24hGraphFragment;
import com.github.simonoppowa.tothemoon_tracker.fragments.PortfolioFragment;
import com.github.simonoppowa.tothemoon_tracker.models.Coin;
import com.github.simonoppowa.tothemoon_tracker.models.CoinAtTime;
import com.github.simonoppowa.tothemoon_tracker.models.Portfolio;
import com.github.simonoppowa.tothemoon_tracker.models.PortfolioAtTime;
import com.github.simonoppowa.tothemoon_tracker.models.Transaction;
import com.github.simonoppowa.tothemoon_tracker.utils.CoinServiceInterface;
import com.github.simonoppowa.tothemoon_tracker.utils.JsonUtils;
import com.google.gson.JsonElement;

import java.util.ArrayList;
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
    private static int callNumber;
    private static int call24hNumber;

    private TransactionDatabase transactionDatabase;
    private CoinServiceInterface mCoinServiceInterface;

    private String mUsedCurrency;
    private List<Coin> mOwnedCoins;
    private List<Transaction> mTransactions;
    private List<List<CoinAtTime>> mCoinsAtTime;

    private PortfolioFragment mPortfolioFragment;
    private Portfolio24hGraphFragment mPortfolioGraphFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up libraries
        Timber.plant(new Timber.DebugTree());
        ButterKnife.bind(this);

        mOwnedCoins = new ArrayList<>();
        mCoinsAtTime = new ArrayList<>();
        setupSharedPreferences();

        transactionDatabase = TransactionDatabase.getDatabase(getApplicationContext());

        // Retrofit API call
        retrofit = new Retrofit.Builder()
                .baseUrl(CRYPTOCOMPARE_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Room DB
        if(mTransactions == null) {
            new DatabaseAsyncTask().execute();
        }




//        final Call<JsonElement> priceCall = coinServiceInterface.getSingleCoinPrice("BTC", mUsedCurrency);
//        priceCall.enqueue(new Callback<JsonElement>() {
//            @Override
//            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
//                //TODO
//                if(response.code() == 200) {
//                    JsonElement jsonElement = response.body();
//                    Timber.d(jsonElement.getAsJsonObject().get("USD").toString());
//                } else {
//                    Timber.d("API Call returned Error: %s", String.valueOf(response.body()));
//                }
//            }
//
//            @Override
//            public void onFailure(Call<JsonElement> call, Throwable t) {
//                Timber.d("Price call failed");
//            }
//        });

        // Fragments UI

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

    private void fetchFullCoinsInfo() {
        if(mTransactions != null) {
            mCoinServiceInterface = retrofit.create(CoinServiceInterface.class);
            callNumber = mTransactions.size();
            call24hNumber = mTransactions.size();

            for(Transaction transaction : mTransactions) {
                Call<JsonElement> coinInfoCall = mCoinServiceInterface.getCoinInfo(transaction.getCoinName(), mUsedCurrency, 0);
                fetchCoinInfo(coinInfoCall);

                Call<JsonElement> coin24hInfoCall = mCoinServiceInterface.get24hCoinChange(transaction.getCoinName(), mUsedCurrency, 24);
                fetchCoin24hInfo(coin24hInfoCall);
            }
        }
    }

    public void fetchCoinInfo(Call call) {
        Timber.d("URL called: %s", String.valueOf(call.request().url()));

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if(response.code() == 200) {
                    JsonElement responseJSON = response.body();
                    Timber.d(responseJSON.toString());

                    Coin newCoin = JsonUtils.getCoinFromResponse(responseJSON);
                    Timber.d("Fetched coin: " + newCoin.getFullName() + ", " + newCoin.getName() +
                            ", " + newCoin.getImageUrl() + ", " + newCoin.getCurrentPrice()
                            + ", " + newCoin.getChange24h() + ", " + newCoin.getChange24hPct());
                    mOwnedCoins.add(newCoin);
                    callNumber--;
                    Timber.d("Calls left: %s", callNumber);
                    if(callNumber == 0) {
                        createPortfolioFragment();
                    }

                } else {
                    Timber.d("API call returned error: %s", String.valueOf(response.body()));
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Timber.d("Coin info call failed");
            }
        });
    }

    public void fetchCoin24hInfo(Call call) {
        Timber.d("URL called: %s", String.valueOf(call.request().url()));

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if(response.code() == 200) {
                    JsonElement responseJson = response.body();
                    Timber.d(responseJson.toString());

                    List<CoinAtTime> newCoinAtTime = JsonUtils.getCoinAtTimeListFromResponse(responseJson, call.request().url().queryParameter("fsym"));
                    call24hNumber--;
                    mCoinsAtTime.add(newCoinAtTime);

                    if(call24hNumber == 0) {
                        List<PortfolioAtTime> portfoliosAtTime = create24hPortfolios(mCoinsAtTime);
                        create24hGraphFragment(portfoliosAtTime);
                    }
                } else {
                    Timber.d("Coin 24h info call failed");
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Timber.d("Coin 24h info call failed");
            }
        });
    }

    private void createPortfolioFragment() {

        // Calculate all necessary numbers
        Portfolio portfolio = calculateTotalPortfolio();
        Timber.d("Portfolio created: " + portfolio.getTotalPrice() + ", " + portfolio.getChange24h() + ", " + portfolio.getChange24hPct());

        // Create Portfolio Fragment
        mPortfolioFragment = PortfolioFragment.newInstance(mUsedCurrency, calculateTotalPortfolio().getTotalPrice(),
                calculateTotalPortfolio().getChange24h(), calculateTotalPortfolio().getChange24hPct());

        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.portfolio_fragment_container, mPortfolioFragment);
        ft.commit();
    }

    private void create24hGraphFragment(List<PortfolioAtTime> portfoliosAtTime) {

        // Create PortfolioGraphFragment
        mPortfolioGraphFragment = Portfolio24hGraphFragment.newInstance(mUsedCurrency, portfoliosAtTime);

        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.portfolio_graph_fragment_container, mPortfolioGraphFragment);
        ft.commit();
    }

    private List<PortfolioAtTime> create24hPortfolios(List<List<CoinAtTime>> coinsAtTime) {
        List<PortfolioAtTime> portfoliosAtTime = new ArrayList<>();

        int i = 0, j = 0;
        while (i < coinsAtTime.get(0).size()) {
            double sum = 0;
            long time = coinsAtTime.get(0).get(i).getAtTime();
            while (j  < coinsAtTime.size()) {
                sum+=coinsAtTime.get(j).get(i).getCurrentPrice();
                j++;
            }
            PortfolioAtTime portfolioAtTime = new PortfolioAtTime(sum, 0, 0, time);
            Timber.d("Created new PortfolioAtTime: "  + sum + ", " + time);
            portfoliosAtTime.add(portfolioAtTime);
            j=0;
            i++;
        }
        return portfoliosAtTime;
    }

    private Portfolio calculateTotalPortfolio() {
        //TODO better search, BigDecimal Calc
        double sum = 0.00;
        double portfolioChange24h = 0.00;
        double portfolioChange24hPct = 0.00;
        for(Coin coin : mOwnedCoins) {
            Transaction coinTransaction = mTransactions
                    .get(mTransactions.indexOf(
                            new Transaction(coin.getName(), 0, 0)));
            sum+=(coin.getCurrentPrice()*coinTransaction.getQuantity());
            portfolioChange24h+=(coin.getChange24h()*coinTransaction.getQuantity());
           double portfolio24hAgo = sum + portfolioChange24h;
           portfolioChange24hPct = (portfolioChange24h / portfolio24hAgo)*100;
        }
        return new Portfolio(sum, portfolioChange24h, portfolioChange24hPct);
    }

    //TODO
    private class DatabaseAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

//            Transaction transaction = new Transaction("ETH", 1, 2.32);
//
//            transactionDatabase.transactionDao().insertTransaction(transaction);

            List<Transaction> transactionList = transactionDatabase.transactionDao().getAllTransactions();

            for(Transaction t : transactionList) {
                Timber.d(t.getCoinName());
            }

            mTransactions = transactionList;
            fetchFullCoinsInfo();

            return null;
        }
    }
}
