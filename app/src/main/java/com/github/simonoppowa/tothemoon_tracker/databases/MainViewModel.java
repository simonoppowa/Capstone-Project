package com.github.simonoppowa.tothemoon_tracker.databases;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.github.simonoppowa.tothemoon_tracker.models.Transaction;

import java.util.List;

import timber.log.Timber;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<Transaction>> transactions;

    public MainViewModel(@NonNull Application application) {
        super(application);
        Timber.plant(new Timber.DebugTree());
        Timber.d("Retrieving data from database");
        TransactionDatabase database = TransactionDatabase.getDatabase(this.getApplication());
        transactions = database.transactionDao().getAllTransactions();
    }

    public LiveData<List<Transaction>> getTransactions() {
        return transactions;
    }

}
