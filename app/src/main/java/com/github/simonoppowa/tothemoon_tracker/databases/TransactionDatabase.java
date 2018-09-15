package com.github.simonoppowa.tothemoon_tracker.databases;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {com.github.simonoppowa.tothemoon_tracker.models.Transaction.class}, version = 3, exportSchema = false)
public abstract class TransactionDatabase extends RoomDatabase {

    public abstract TransactionDao transactionDao();

    private static volatile TransactionDatabase INSTANCE;

    public static TransactionDatabase getDatabase(final Context context) {
        if(INSTANCE == null) {
            synchronized (TransactionDatabase.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            TransactionDatabase.class, "transactionsDB")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
