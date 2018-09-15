package com.github.simonoppowa.tothemoon_tracker.databases;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.github.simonoppowa.tothemoon_tracker.models.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTransaction(Transaction transaction);

    @Query("SELECT * FROM transaction_table")
    List<Transaction> getAllTransactions();

    @Query("SELECT * FROM transaction_table WHERE coin_name =:coinName")
    Transaction getSingleTransaction(String coinName);

    @Delete
    void deleteTransaction(Transaction transaction);

    @Query("DELETE FROM transaction_table")
    void deleteAllTransactions();
}
