package com.github.simonoppowa.tothemoon_tracker.databases;

import android.os.AsyncTask;

import com.github.simonoppowa.tothemoon_tracker.models.Transaction;

import java.util.List;

import timber.log.Timber;

public class GetDatabaseAsyncTask extends AsyncTask<TransactionDatabase, Void, List<Transaction>> {

    private OnDatabaseTaskCompleted mListener;

    public interface OnDatabaseTaskCompleted {
        /**
         * Gets called when GetDataBaseAsyncTask is finished
         * @param transactions Transactions loaded from Database
         */
        void onDatabaseTaskCompleted(List<Transaction> transactions);
    }

    public GetDatabaseAsyncTask(OnDatabaseTaskCompleted listener) {
        mListener = listener;
    }

    @Override
    protected List<Transaction> doInBackground(TransactionDatabase... transactionDatabases) {

        List<Transaction> transactionList = transactionDatabases[0].transactionDao().getAllTransactions();

        for(Transaction t : transactionList) {
            Timber.d(t.getCoinName());
        }

        mListener.onDatabaseTaskCompleted(transactionList);

        return transactionList;
    }
}
