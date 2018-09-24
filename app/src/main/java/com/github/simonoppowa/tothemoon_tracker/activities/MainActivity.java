package com.github.simonoppowa.tothemoon_tracker.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.simonoppowa.tothemoon_tracker.R;
import com.github.simonoppowa.tothemoon_tracker.adapters.CoinSearchAdapter;
import com.github.simonoppowa.tothemoon_tracker.databases.TransactionDatabase;
import com.github.simonoppowa.tothemoon_tracker.fragments.CoinsInfoFragment;
import com.github.simonoppowa.tothemoon_tracker.fragments.Portfolio24hGraphFragment;
import com.github.simonoppowa.tothemoon_tracker.fragments.PortfolioFragment;
import com.github.simonoppowa.tothemoon_tracker.fragments.PortfolioPieChartFragment;
import com.github.simonoppowa.tothemoon_tracker.models.Coin;
import com.github.simonoppowa.tothemoon_tracker.models.CoinAtTime;
import com.github.simonoppowa.tothemoon_tracker.models.Portfolio;
import com.github.simonoppowa.tothemoon_tracker.models.PortfolioAtTime;
import com.github.simonoppowa.tothemoon_tracker.models.Transaction;
import com.github.simonoppowa.tothemoon_tracker.services.CoinSearchDialogCompat;
import com.github.simonoppowa.tothemoon_tracker.services.CoinServiceInterface;
import com.github.simonoppowa.tothemoon_tracker.utils.JsonUtils;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import ir.mirrajabi.searchdialog.core.BaseSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;
import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    public static final String CRYPTOCOMPARE_API_BASE_URL = "https://min-api.cryptocompare.com/data/";
    public static final String CRYPTOCOMPARE_BASE_URL = "https://www.cryptocompare.com/";

    public static final String DEFAULT_CURRENCY = "USD";

    private static Retrofit retrofit;

    private TransactionDatabase transactionDatabase;
    private CoinServiceInterface mCoinServiceInterface;

    private String mUsedCurrency;
    private List<Coin> mOwnedCoins;
    private ArrayList<Coin> mCoinsAvailable;
    private List<Transaction> mTransactions;
    private List<List<CoinAtTime>> mCoinsAtTime;

    private PortfolioFragment mPortfolioFragment;
    private Portfolio24hGraphFragment mPortfolioGraphFragment;
    private CoinsInfoFragment mCoinsInfoFragment;
    private PortfolioPieChartFragment mPortfolioPieChartFragment;
    @BindView(R.id.main_toolbar)
    Toolbar mMainToolbar;

    //TODO delete

    private ArrayList<Coin> createSampleData() {
        ArrayList<Coin> items = new ArrayList<>();
        items.add(new Coin("ETH", "Ethereum", "https://randomuser.me/api/portraits/women/93.jpg", 0, 0, 0));
        items.add(new Coin("BTC", "Bitcoin", "https://randomuser.me/api/portraits/women/93.jpg", 0, 0, 0));
        items.add(new Coin("XRP", "Ripple", "https://randomuser.me/api/portraits/women/93.jpg", 0, 0, 0));
        items.add(new Coin("LTC", "Litecoin", "https://randomuser.me/api/portraits/women/93.jpg", 0, 0, 0));
        return items;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up libraries
        Timber.plant(new Timber.DebugTree());
        ButterKnife.bind(this);

        setSupportActionBar(mMainToolbar);

        mOwnedCoins = new ArrayList<>();
        mCoinsAtTime = new ArrayList<>();
        setupSharedPreferences();

        transactionDatabase = TransactionDatabase.getDatabase(getApplicationContext());

        // Retrofit API call
        retrofit = new Retrofit.Builder()
                .baseUrl(CRYPTOCOMPARE_API_BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // Room DB
        if(mTransactions == null) {
            new DatabaseAsyncTask().execute();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);


        // Set icon color
        for(int i = 0; i < menu.size(); i++){
            Drawable drawable = menu.getItem(i).getIcon();
            if(drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.defaultTextColor), PorterDuff.Mode.SRC_ATOP);
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemSelected = item.getItemId();

        if(itemSelected == R.id.settings_item) {
            Timber.d("Settings selected");
            startPreferencesActivity();
        } else {
            Timber.d("Search selected");
            provideCustomDialog();
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    void provideCustomDialog() {
        mCoinsAvailable = new ArrayList<>();

        CoinSearchDialogCompat compatDialog = new CoinSearchDialogCompat<>(this, "Search...", "What are you looking for...", null, mCoinsAvailable,
                new SearchResultListener<Coin>() {

                    @Override
                    public void onSelected(BaseSearchDialogCompat dialog, Coin coin, int position) {
                        Toast.makeText(MainActivity.this, coin.getTitle(),
                                Toast.LENGTH_SHORT
                        ).show();
                        dialog.dismiss();
                    }
                });

        compatDialog.show();
        compatDialog.setLoading(true);

        fetchFullCoinList(compatDialog);


    }

    @SuppressLint("ApplySharedPref")
    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(sharedPreferences.contains(getString(R.string.pref_currency_key))) {
            mUsedCurrency = sharedPreferences.getString(getString(R.string.pref_currency_key), DEFAULT_CURRENCY);
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.pref_currency_key), DEFAULT_CURRENCY);
            editor.commit();
            mUsedCurrency = DEFAULT_CURRENCY;
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void fetchFullCoinsInfo() {
        if(mTransactions != null) {
            mCoinServiceInterface = retrofit.create(CoinServiceInterface.class);

            List<Observable<?>> coinInfoRequests = new ArrayList<>();
            List<Observable<?>> coin24hInfoRequest = new ArrayList<>();

            for(Transaction transaction : mTransactions) {
                Observable<JsonElement> coinInfoCall = mCoinServiceInterface.getCoinInfo(transaction.getCoinName(), mUsedCurrency, 0);
                coinInfoRequests.add(coinInfoCall);

                Observable<Response<JsonElement>> coin24hInfoCall = mCoinServiceInterface.get24hCoinChange(transaction.getCoinName(), mUsedCurrency, 24);
                coin24hInfoRequest.add(coin24hInfoCall);
            }

            fetchCoinsInfo(coinInfoRequests);

            fetchCoins24hInfo(coin24hInfoRequest);
        }
    }

    public void fetchCoinsInfo(final List<Observable<?>> coinInfoRequests) {

        // Use RxJava2 to coordinate coinInfoCalls
        Observable.zip(coinInfoRequests,
                new Function<Object[], Object>() {

                    @Override
                    public Object apply(Object[] objects) {
                        // Objects[] is an array of combined results of completed requests

                        for(Object response : objects) {
                            Coin newCoin = JsonUtils.getCoinFromResponse((JsonElement) response);

                            Timber.d("Fetched coin: " + newCoin.getFullName() + ", " + newCoin.getName() +
                                    ", " + newCoin.getImageUrl() + ", " + newCoin.getCurrentPrice()
                                    + ", " + newCoin.getChange24h() + ", " + newCoin.getChange24hPct());
                            mOwnedCoins.add(newCoin);
                        }

                        Portfolio currentPortfolio = calculateTotalPortfolio();

                        return currentPortfolio;
                    }
                })
                .subscribe(
                        new Consumer<Object>() {
                            @Override
                            public void accept(Object currentPortfolio) throws Exception {
                                // Successful completion of all requests

                                createPortfolioFragment((Portfolio) currentPortfolio);
                                createCoinsInfoFragment();
                                createPieChartFragment((Portfolio) currentPortfolio);
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                // Error completion of requests
                                throwable.printStackTrace();
                            }
                        }
                );
    }

    public void fetchCoins24hInfo(List<Observable<?>> coin24hInfoRequest) {

        // Use RxJava2 to coordinate coin24hInfoCalls
        Observable.zip(
                coin24hInfoRequest,
                new Function<Object[], Object>() {

                    @Override
                    public Object apply(Object[] objects) {

                        for(Object response : objects) {

                            Response<JsonElement> responseJson = (Response<JsonElement>) response;

                            HttpUrl url = ((Response<JsonElement>) response).raw().request().url();

                            JsonElement jsonElement = ((Response<JsonElement>) response).body();

                            List<CoinAtTime> newCoinAtTime = JsonUtils.getCoinAtTimeListFromResponse(jsonElement, url.queryParameter("fsym"));
                            mCoinsAtTime.add(newCoinAtTime);
                        }
                        return mCoinsAtTime;
                    }
                })
                .subscribe(
                        new Consumer<Object>() {
                            @Override
                            public void accept(Object o) throws Exception {
                                List<PortfolioAtTime> portfoliosAtTime = create24hPortfolios(mCoinsAtTime);
                                create24hGraphFragment(portfoliosAtTime);
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                throwable.printStackTrace();
                            }
                        }
                );
    }

    private void fetchFullCoinList(final CoinSearchDialogCompat dialogCompat) {

        CoinServiceInterface coinServiceInterface = retrofit.create(CoinServiceInterface.class);

        Call<JsonElement> call = coinServiceInterface.getFullCoinList();
        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                mCoinsAvailable = (ArrayList<Coin>) JsonUtils.getCoinListFromResponse(response.body());

                CoinSearchAdapter coinSearchAdapter = (CoinSearchAdapter) dialogCompat.getAdapter();
                coinSearchAdapter.setItems(mCoinsAvailable);
                dialogCompat.setLoading(false);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });
    }

    private void createPortfolioFragment(Portfolio currentPortfolio) {

        // Calculate all necessary numbers
        Portfolio portfolio = calculateTotalPortfolio();
        Timber.d("Portfolio created: " + portfolio.getTotalPrice() + ", " + portfolio.getChange24h() + ", " + portfolio.getChange24hPct());

        // Create Portfolio Fragment
        mPortfolioFragment = PortfolioFragment.newInstance(mUsedCurrency, currentPortfolio.getTotalPrice(),
                currentPortfolio.getChange24h(), currentPortfolio.getChange24hPct());

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

    private void createCoinsInfoFragment() {

        // Create CoinsInfoFragment
        mCoinsInfoFragment = CoinsInfoFragment.newInstance(mUsedCurrency, (ArrayList<Coin>) mOwnedCoins);

        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.coins_info_fragment_container, mCoinsInfoFragment);
        ft.commit();
    }

    private void createPieChartFragment(Portfolio portfolio) {

        // Create CoinsInfoFragment
        mPortfolioPieChartFragment = PortfolioPieChartFragment.newInstance(mUsedCurrency, portfolio.getTotalPrice(), (ArrayList<Coin>) mOwnedCoins);
        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.portfolio_pie_chart_fragment_container, mPortfolioPieChartFragment);
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

    private void startPreferencesActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_currency_key))) {
            mUsedCurrency = (getString(R.string.pref_currency_key));
            Timber.d("Used Currency changed to %s", mUsedCurrency);
        }
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
