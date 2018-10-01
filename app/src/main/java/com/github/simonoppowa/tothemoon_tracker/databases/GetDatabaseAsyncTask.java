package com.github.simonoppowa.tothemoon_tracker.databases;

import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;

import com.github.simonoppowa.tothemoon_tracker.models.Transaction;

import java.util.List;

public class GetDatabaseAsyncTask extends AsyncTask<TransactionDatabase, Void, LiveData<List<Transaction>>> {

    private OnDatabaseTaskCompleted mListener;

    public interface OnDatabaseTaskCompleted {
        /**
         * Gets called when GetDataBaseAsyncTask is finished
         * @param transactions Transactions loaded from Database
         */
        void onDatabaseTaskCompleted(LiveData<List<Transaction>> transactions);
    }

    public GetDatabaseAsyncTask(OnDatabaseTaskCompleted listener) {
        mListener = listener;
    }

    @Override
    protected LiveData<List<Transaction>> doInBackground(TransactionDatabase... transactionDatabases) {

        LiveData<List<Transaction>> transactionList = transactionDatabases[0].transactionDao().getAllTransactions();

        mListener.onDatabaseTaskCompleted(transactionList);

        return transactionList;
    }
}
