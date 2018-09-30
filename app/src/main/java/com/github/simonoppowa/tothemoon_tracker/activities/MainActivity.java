package com.github.simonoppowa.tothemoon_tracker.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.simonoppowa.tothemoon_tracker.R;
import com.github.simonoppowa.tothemoon_tracker.adapters.CoinSearchAdapter;
import com.github.simonoppowa.tothemoon_tracker.databases.GetDatabaseAsyncTask;
import com.github.simonoppowa.tothemoon_tracker.databases.TransactionDatabase;
import com.github.simonoppowa.tothemoon_tracker.fragments.CoinsInfoFragment;
import com.github.simonoppowa.tothemoon_tracker.fragments.Portfolio24hGraphFragment;
import com.github.simonoppowa.tothemoon_tracker.fragments.PortfolioFragment;
import com.github.simonoppowa.tothemoon_tracker.fragments.PortfolioPieChartFragment;
import com.github.simonoppowa.tothemoon_tracker.fragments.RocketImageFragment;
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
import ir.mirrajabi.searchdialog.SimpleSearchFilter;
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

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
        GetDatabaseAsyncTask.OnDatabaseTaskCompleted, SwipeRefreshLayout.OnRefreshListener {

    public static final String CRYPTOCOMPARE_API_BASE_URL = "https://min-api.cryptocompare.com/data/";
    public static final String CRYPTOCOMPARE_BASE_URL = "https://www.cryptocompare.com/";

    public static final int MAX_API_CALLS = 10;
    public static final String DEFAULT_CURRENCY = "USD";

    private static Retrofit retrofit;
    private Handler mHandler;

    private TransactionDatabase transactionDatabase;
    private CoinServiceInterface mCoinServiceInterface;

    private String mUsedCurrency;
    private List<Coin> mOwnedCoins;
    private ArrayList<Coin> mCoinsAvailable;
    private List<Transaction> mTransactions;

    private boolean mRefreshOnResume;

    private RocketImageFragment mRocketImageFragment;
    private PortfolioFragment mPortfolioFragment;
    private Portfolio24hGraphFragment mPortfolioGraphFragment;
    private CoinsInfoFragment mCoinsInfoFragment;
    private PortfolioPieChartFragment mPortfolioPieChartFragment;
    @BindView(R.id.main_toolbar)
    Toolbar mMainToolbar;
    @BindView(R.id.swipe_container)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up libraries
        Timber.plant(new Timber.DebugTree());
        ButterKnife.bind(this);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        setSupportActionBar(mMainToolbar);
        mMainToolbar.setTitle(R.string.app_name_short);
        mMainToolbar.setTitleTextColor(getResources().getColor(R.color.defaultTextColor));

        mOwnedCoins = new ArrayList<>();
        setupSharedPreferences();

        transactionDatabase = TransactionDatabase.getDatabase(getApplicationContext());

        // Retrofit
        retrofit = new Retrofit.Builder()
                .baseUrl(CRYPTOCOMPARE_API_BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        if (savedInstanceState != null) {
            return;
        }

        if (mTransactions == null) {
            new GetDatabaseAsyncTask(this).execute(transactionDatabase);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        // Set icon color
        for (int i = 0; i < menu.size(); i++) {
            Drawable drawable = menu.getItem(i).getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(getResources().getColor(R.color.defaultTextColor), PorterDuff.Mode.SRC_ATOP);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemSelected = item.getItemId();

        if (itemSelected == R.id.settings_item) {
            Timber.d("Settings selected");
            startPreferencesActivity();
        } else {
            Timber.d("Search selected");
            provideCustomDialog();
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRefreshOnResume) {
            refresh();
            mRefreshOnResume = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    void provideCustomDialog() {
        mCoinsAvailable = new ArrayList<>();

        CoinSearchDialogCompat compatDialog = new CoinSearchDialogCompat<>(this,
                getString(R.string.search_coin_title), getString(R.string.search_coin_hint), null, mCoinsAvailable,
                new SearchResultListener<Coin>() {

                    @Override
                    public void onSelected(BaseSearchDialogCompat dialog, Coin coin, int position) {
                        if(mTransactions.size() > MAX_API_CALLS) {
                            Toast.makeText(MainActivity.this, getString(R.string.error_max_transactions), Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            startAddCoinActivity(coin);
                        }
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
        if (sharedPreferences.contains(getString(R.string.pref_currency_key))) {
            mUsedCurrency = sharedPreferences.getString(getString(R.string.pref_currency_key), DEFAULT_CURRENCY);
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(getString(R.string.pref_currency_key), DEFAULT_CURRENCY);
            editor.commit();
            mUsedCurrency = DEFAULT_CURRENCY;
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    public void refresh() {
        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();

        // Remove all fragments
        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            ft.remove(fragment);
        }
        ft.commit();
        mOwnedCoins = new ArrayList<>();

        new GetDatabaseAsyncTask(this).execute(transactionDatabase);
    }

    private void fetchFullCoinsInfo() {
        if (mTransactions != null) {
            mCoinServiceInterface = retrofit.create(CoinServiceInterface.class);

            List<Observable<?>> coinInfoRequests = new ArrayList<>();
            List<Observable<?>> coin24hInfoRequest = new ArrayList<>();

            for (Transaction transaction : mTransactions) {
                Observable<JsonElement> coinInfoCall = mCoinServiceInterface.getCoinInfo(transaction.getCoinName(), mUsedCurrency, 0);
                coinInfoRequests.add(coinInfoCall);

                Observable<Response<JsonElement>> coin24hInfoCall = mCoinServiceInterface.get24hCoinChange(transaction.getCoinName(), mUsedCurrency, 23);
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
                        for (Object response : objects) {
                            Coin newCoin = JsonUtils.getCoinFromResponse((JsonElement) response);

                            Timber.d("Fetched coin: " + newCoin.getFullName() + ", " + newCoin.getName() +
                                    ", " + newCoin.getImageUrl() + ", " + newCoin.getCurrentPrice()
                                    + ", " + newCoin.getChange24h() + ", " + newCoin.getChange24hPct());
                            mOwnedCoins.add(newCoin);
                        }

                        return calculateTotalPortfolio(mOwnedCoins, mTransactions);
                    }
                })
                .subscribe(
                        new Consumer<Object>() {
                            @Override
                            public void accept(Object currentPortfolio) {
                                // Successful completion of all requests
                                createRocketImageFragment(((Portfolio) currentPortfolio).getChange24hPct());
                                createPortfolioFragment((Portfolio) currentPortfolio);
                                createCoinsInfoFragment();
                                createPieChartFragment();
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                // Error completion of requests
                                Timber.d("Error while fetching coins info");
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
                        List<List<CoinAtTime>> coinsAtTime = new ArrayList<>();

                        for (Object response : objects) {

                            HttpUrl url = ((Response<JsonElement>) response).raw().request().url();

                            JsonElement jsonElement = ((Response<JsonElement>) response).body();

                            List<CoinAtTime> newCoinAtTime = JsonUtils.getCoinAtTimeListFromResponse(jsonElement, url.queryParameter("fsym"));
                            coinsAtTime.add(newCoinAtTime);
                        }
                        return coinsAtTime;
                    }
                })
                .subscribe(
                        new Consumer<Object>() {
                            @Override
                            public void accept(Object o) {
                                List<List<CoinAtTime>> coinsAtTime = (List<List<CoinAtTime>>) o;
                                List<PortfolioAtTime> portfoliosAtTime = create24hPortfolios(coinsAtTime);
                                create24hGraphFragment(portfoliosAtTime);
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                Timber.d("Error while fetching 24h coins info");
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

                dialogCompat.setFilter(new SimpleSearchFilter<>(mCoinsAvailable, dialogCompat.getFilterResultListener()));
                dialogCompat.setLoading(false);
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Timber.d("Error while fetching coin list");
            }
        });
    }

    private void createRocketImageFragment(double change24hPct) {
        mRocketImageFragment = RocketImageFragment.newInstance(change24hPct);

        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.rocket_image_fragment_container, mRocketImageFragment);
        ft.commit();
    }

    private void createPortfolioFragment(Portfolio currentPortfolio) {

        // Calculate all necessary numbers
        Portfolio portfolio = calculateTotalPortfolio(mOwnedCoins, mTransactions);
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
        mCoinsInfoFragment = CoinsInfoFragment.newInstance(mUsedCurrency, (ArrayList<Coin>) mOwnedCoins, mTransactions);
        FragmentManager fm = getSupportFragmentManager();

        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.coins_info_fragment_container, mCoinsInfoFragment);
        ft.commit();
    }

    private void createPieChartFragment() {

        // Create CoinsInfoFragment
        mPortfolioPieChartFragment = PortfolioPieChartFragment
                .newInstance((ArrayList<Coin>) mOwnedCoins, mTransactions);
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
            while (j < coinsAtTime.size()) {
                Transaction coinTransaction = mTransactions.get(mTransactions.indexOf(new Transaction(coinsAtTime.get(j).get(0).getName(),
                        0, 0)));
                sum += (coinsAtTime.get(j).get(i).getCurrentPrice() * coinTransaction.getQuantity());
                j++;
            }
            PortfolioAtTime portfolioAtTime = new PortfolioAtTime(sum, 0, 0, time);
            Timber.d("Created new PortfolioAtTime: " + sum + ", " + time);
            portfoliosAtTime.add(portfolioAtTime);
            j = 0;
            i++;
        }
        return portfoliosAtTime;
    }

    public static Portfolio calculateTotalPortfolio(List<Coin> coinList, List<Transaction> transactionList) {
        double sum = 0.00;
        double portfolioChange24h = 0.00;
        double portfolioChange24hPct = 0.00;
        for (Coin coin : coinList) {
            Transaction coinTransaction = transactionList.get(transactionList.indexOf(new Transaction(coin.getName(),
                    0, 0)));
            sum += (coin.getCurrentPrice() * coinTransaction.getQuantity());
            portfolioChange24h += (coin.getChange24h() * coinTransaction.getQuantity());
            double portfolio24hAgo = sum + portfolioChange24h;
            portfolioChange24hPct = (portfolioChange24h / portfolio24hAgo) * 100;
        }
        return new Portfolio(sum, portfolioChange24h, portfolioChange24hPct);
    }

    private void startPreferencesActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void startAddCoinActivity(Coin selectedCoin) {
        Intent intent = new Intent(this, AddTransactionActivity.class);
        intent.putExtra(AddTransactionActivity.COIN_KEY, selectedCoin);

        startActivity(intent);
    }

    private void setupDemoPortfolio() {
        // Insert transactions in new thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                Transaction exampleTransaction1 = new Transaction("ETH", 220, 2);
                Transaction exampleTransaction2 = new Transaction("BTC", 5000, 0.5);
                Transaction exampleTransaction3 = new Transaction("XMR", 110, 10);
                Transaction exampleTransaction4 = new Transaction("LTC", 50, 10);

                transactionDatabase.transactionDao().insertTransaction(exampleTransaction1);
                transactionDatabase.transactionDao().insertTransaction(exampleTransaction2);
                transactionDatabase.transactionDao().insertTransaction(exampleTransaction3);
                transactionDatabase.transactionDao().insertTransaction(exampleTransaction4);

                refresh();
            }
        }).start();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_currency_key))) {
            mUsedCurrency = sharedPreferences.getString(key, getString(R.string.pref_currency_value_usd));
            Timber.d("Used Currency changed to %s", mUsedCurrency);
            mRefreshOnResume = true;
        }
    }

    @Override
    public void onDatabaseTaskCompleted(List<Transaction> transactions) {
        mTransactions = transactions;

        if (mTransactions.size() == 0) {
            // Show dialog in ui thread
            mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    MaterialStyledDialog dialog = new MaterialStyledDialog.Builder(MainActivity.this)
                            .setTitle(getString(R.string.welcome_dialog_title))
                            .setDescription(getString(R.string.welcome_dialog_description))
                            .withIconAnimation(false)
                            .withDarkerOverlay(true)
                            .setIcon(R.drawable.ic_rocket)
                            .setNegativeText(getString(R.string.welcome_dialog_button_left))
                            .setPositiveText(getString(R.string.welcome_dialog_button_right))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    setupDemoPortfolio();
                                }
                            })
                            .build();
                    dialog.show();
                }
            };

            Message message = mHandler.obtainMessage();
            message.sendToTarget();

        } else {
            fetchFullCoinsInfo();
        }
    }

    @Override
    public void onRefresh() {
        refresh();
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
