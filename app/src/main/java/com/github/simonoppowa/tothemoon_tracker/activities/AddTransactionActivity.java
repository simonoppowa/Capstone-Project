package com.github.simonoppowa.tothemoon_tracker.activities;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.github.simonoppowa.tothemoon_tracker.R;
import com.github.simonoppowa.tothemoon_tracker.databases.TransactionDatabase;
import com.github.simonoppowa.tothemoon_tracker.models.Coin;
import com.github.simonoppowa.tothemoon_tracker.models.Transaction;
import com.github.simonoppowa.tothemoon_tracker.utils.NumberFormatUtils;
import com.github.simonoppowa.tothemoon_tracker.utils.PicassoUtils;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import timber.log.Timber;

public class AddTransactionActivity extends AppCompatActivity {

    public static final String COIN_KEY = "coinKey";

    @BindView(R.id.add_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.add_coin_image_view)
    RoundedImageView mCoinImageView;
    @BindView(R.id.add_coin_name_text_view)
    TextView mCoinTitleTextView;
    @BindView(R.id.add_trade_price_edit_text)
    TextInputEditText mTradePriceEditText;
    @BindView(R.id.add_quantity_edit_text)
    TextInputEditText mQuantityEditText;
    @BindView(R.id.add_full_price)
    TextView mFullPriceTextView;

    private Coin mSelectedCoin;

    private BigDecimal mTradePriceInput;
    private BigDecimal mQuantityInput;
    private BigDecimal mFullPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Set up libraries
        Timber.plant(new Timber.DebugTree());
        ButterKnife.bind(this);

        // Set up Toolbar
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        setTitle(getString(R.string.add_transaction_activity_title));

        mTradePriceInput = new BigDecimal(0);
        mQuantityInput = new BigDecimal(0);
        mFullPrice = new BigDecimal(0);

        // Get Coin Intent
        Intent intent = getIntent();
        mSelectedCoin = intent.getParcelableExtra(COIN_KEY);

        if(mSelectedCoin == null) {
            throw new NullPointerException("No coin was passed to AddTransactionActivity");
        }

        populateUI();
    }

    private void populateUI() {
        Picasso.get()
                .load(PicassoUtils.getFullCoinImageUrl(mSelectedCoin.getImageUrl()))
                .into(mCoinImageView);

        mCoinTitleTextView.setText(mSelectedCoin.getFullName());
    }

    @OnTextChanged(R.id.add_trade_price_edit_text)
    void onTradePriceTextChanged(CharSequence charSequence) {
        if(charSequence != null && !charSequence.toString().equals("")) {
            mTradePriceInput = new BigDecimal(charSequence.toString());
            setFullPriceTextView();
        }
    }

    @OnTextChanged(R.id.add_quantity_edit_text)
    void onQuantityTextChanged(CharSequence charSequence) {
        if(charSequence != null && !charSequence.toString().equals("")) {
            mQuantityInput = new BigDecimal(charSequence.toString());
            setFullPriceTextView();
        }
    }

    private void setFullPriceTextView() {
        mFullPrice = mTradePriceInput.multiply(mQuantityInput);
        mFullPriceTextView.setText(NumberFormatUtils.format2Decimal(mFullPrice.doubleValue()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_transaction_menu, menu);

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
        if(item.getItemId() == android.R.id.home) {
            // Back button pressed
            onBackPressed();
            return true;
        } else {
            // Save button pressed
            if(isQuantityValid() && isTradePriceValid()) {
                addNewTransactionToDatabase();
            } else {
                Toast.makeText(this, getString(R.string.input_error), Toast.LENGTH_SHORT).show();
            }
        }

        return false;
    }

    private boolean isQuantityValid() {
        return !mQuantityInput.toString().equals("0");
    }

    private boolean isTradePriceValid() {
        return !mTradePriceInput.toString().equals("0");
    }

    private void addNewTransactionToDatabase() {

        new InsertInDatabaseAsyncTask().execute();
        finish();
    }

    public class InsertInDatabaseAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Transaction newTransaction = new Transaction(mSelectedCoin.getName(), mTradePriceInput, mQuantityInput);

            TransactionDatabase transactionDatabase = TransactionDatabase.getDatabase(getApplicationContext());

            transactionDatabase.transactionDao().insertTransaction(newTransaction);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Toast.makeText(AddTransactionActivity.this, mSelectedCoin.getName() + " transaction added", Toast.LENGTH_SHORT).show();
        }
    }
}
