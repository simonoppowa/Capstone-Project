package com.github.simonoppowa.tothemoon_tracker.widgets;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.github.simonoppowa.tothemoon_tracker.R;
import com.github.simonoppowa.tothemoon_tracker.activities.MainActivity;
import com.github.simonoppowa.tothemoon_tracker.databases.GetDatabaseAsyncTask;
import com.github.simonoppowa.tothemoon_tracker.databases.TransactionDatabase;
import com.github.simonoppowa.tothemoon_tracker.models.Coin;
import com.github.simonoppowa.tothemoon_tracker.models.Portfolio;
import com.github.simonoppowa.tothemoon_tracker.models.Transaction;
import com.github.simonoppowa.tothemoon_tracker.services.CoinServiceInterface;
import com.github.simonoppowa.tothemoon_tracker.utils.JsonUtils;
import com.github.simonoppowa.tothemoon_tracker.utils.NumberFormatUtils;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

import static com.github.simonoppowa.tothemoon_tracker.activities.MainActivity.CRYPTOCOMPARE_API_BASE_URL;
import static com.github.simonoppowa.tothemoon_tracker.activities.MainActivity.DEFAULT_CURRENCY;

/**
 * Implementation of App Widget functionality.
 */
public class PortfolioWidget extends AppWidgetProvider implements GetDatabaseAsyncTask.OnDatabaseTaskCompleted, SharedPreferences.OnSharedPreferenceChangeListener{

    private Retrofit retrofit;
    private Context mContext;

    private String mUsedCurrency;

    RemoteViews mViews;
    AppWidgetManager mAppWidgetManager;


    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager) {

        // Set up libraries
        Timber.plant(new Timber.DebugTree());

        mContext = context;
        mAppWidgetManager = appWidgetManager;

        setupSharedPreferences();

        TransactionDatabase transactionDatabase = TransactionDatabase.getDatabase(context);

        new GetDatabaseAsyncTask(this).execute(transactionDatabase);

        mViews = new RemoteViews(context.getPackageName(), R.layout.portfolio_widget);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }


    @Override
    public void onDatabaseTaskCompleted(final List<Transaction> transactions) {

        // Retrofit API call
        retrofit = new Retrofit.Builder()
                .baseUrl(CRYPTOCOMPARE_API_BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        CoinServiceInterface coinServiceInterface = retrofit.create(CoinServiceInterface.class);

        final List<Observable<?>> coinInfoRequests = new ArrayList<>();

        for(Transaction transaction : transactions) {
            Observable<JsonElement> coinInfoCall = coinServiceInterface.getCoinInfo(transaction.getCoinName(), mUsedCurrency, 0);
            coinInfoRequests.add(coinInfoCall);
        }


        // Use RxJava2 to coordinate coinInfoCalls
        Observable.zip(coinInfoRequests,
                new Function<Object[], Object>() {

                    @Override
                    public Object apply(Object[] objects) {
                        // Objects[] is an array of combined results of completed requests

                        List<Coin> coinList = new ArrayList<>();

                        for(Object response : objects) {
                            Coin newCoin = JsonUtils.getCoinFromResponse((JsonElement) response);

                            Timber.d("Fetched coin: " + newCoin.getFullName() + ", " + newCoin.getName() +
                                    ", " + newCoin.getImageUrl() + ", " + newCoin.getCurrentPrice()
                                    + ", " + newCoin.getChange24h() + ", " + newCoin.getChange24hPct());
                            coinList.add(newCoin);
                        }

                        return MainActivity.calculateTotalPortfolio(coinList, transactions);
                    }
                })
                .subscribe(
                        new Consumer<Object>() {
                            @Override
                            public void accept(Object currentPortfolio) {
                                // Successful completion of all requests
                                Portfolio portfolio = (Portfolio) currentPortfolio;

                                String totalPrice = NumberFormatUtils.format2Decimal(portfolio.getTotalPrice());
                                String change24h = NumberFormatUtils.format2Decimal(portfolio.getChange24h());
                                String change24hPct = NumberFormatUtils.format2Decimal(portfolio.getChange24hPct());

                                mViews.setTextViewText(R.id.widget_total_portfolio_price_text_view, mUsedCurrency + " " + totalPrice);
                                mViews.setTextViewText(R.id.widget_daily_change_24h_text_view, mUsedCurrency + " " + change24h);
                                mViews.setTextViewText(R.id.widget_daily_change_24h_pct_text_view, change24hPct + "%");

                                mViews.setImageViewResource(R.id.widget_image_view, R.drawable.ic_rocket);

                                ComponentName thisWidget = new ComponentName(mContext, PortfolioWidget.class);

                                // Instruct the widget manager to update the widget
                                mAppWidgetManager.updateAppWidget(thisWidget, mViews);
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                // Error completion of requests
                                throwable.printStackTrace();
                            }
                        }
                );
    }

    @SuppressLint("ApplySharedPref")
    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        String currency = mContext.getString(R.string.pref_currency_key);

        if(sharedPreferences.contains(currency)) {
            mUsedCurrency = sharedPreferences.getString(currency, DEFAULT_CURRENCY);
        } else {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(currency, DEFAULT_CURRENCY);
            editor.commit();
            mUsedCurrency = DEFAULT_CURRENCY;
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }
}

