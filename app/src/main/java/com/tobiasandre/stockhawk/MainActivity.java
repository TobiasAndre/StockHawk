package com.tobiasandre.stockhawk;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.tobiasandre.stockhawk.R;
import com.tobiasandre.stockhawk.data.CotacaoCursorAdapter;
import com.tobiasandre.stockhawk.data.CotacaoProvider;
import com.tobiasandre.stockhawk.data.CotacaoTable;
import com.tobiasandre.stockhawk.data.RecyclerViewItemClickListener;
import com.tobiasandre.stockhawk.stockwidget.ItemTouchHelperCallback;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Tobias Andre Eggers on 4/10/17.
 */

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>, RecyclerViewItemClickListener.OnItemClickListener {

    public static final int UNIDADE_DOLARES = 0;
    public static final int UNIDADE_PERCENTUAL = 1;
    private static final int CURSOR_LOADER_ID = 0;
    private final String EXTRA_UNIDADES = "EXTRA_UNIDADES";
    private final String EXTRA_ADD_DIALOGO = "EXTRA_ADD_DIALOGO";

    private int mUnidade = UNIDADE_DOLARES;
    private CotacaoCursorAdapter mAdapter;
    private MaterialDialog mDialogo;

    @Bind(R.id.stock_list)
    RecyclerView mRecyclerView;
    @Bind(R.id.sem_conexao)
    View mSemConexao;
    @Bind(R.id.sem_cotacoes)
    View mSemCotacoes;
    @Bind(R.id.progress)
    ProgressBar mProgressBar;
    @Bind(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        if (savedInstanceState == null) {
            Intent stackServiceIntent = new Intent(this, CotacaoIntentService.class);
            stackServiceIntent.putExtra(CotacaoIntentService.EXTRA_TAG, CotacaoIntentService.ACTION_INIT);
            if (isNetworkAvailable()) {
                startService(stackServiceIntent);
            } else {
                Snackbar.make(mCoordinatorLayout, getString(R.string.sem_conexao_internet),
                        Snackbar.LENGTH_LONG).show();
            }
        } else {
            mUnidade = savedInstanceState.getInt(EXTRA_UNIDADES);
            if (savedInstanceState.getBoolean(EXTRA_ADD_DIALOGO, false)) {
                showDialogAdicionandoCotacao();
            }
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this, this));

        mAdapter = new CotacaoCursorAdapter(this, null, mUnidade);
        mRecyclerView.setAdapter(mAdapter);

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        ItemTouchHelper.Callback callback = new ItemTouchHelperCallback(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        PeriodicTask periodicTask = new PeriodicTask.Builder()
                .setService(CotacaoTaskService.class)
                .setPeriod(/* 1h */ 60 * 60)
                .setFlex(/* 10s */ 10)
                .setTag(CotacaoTaskService.TAG_PERIODIC)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setRequiresCharging(false)
                .build();

        GcmNetworkManager.getInstance(this).schedule(periodicTask);

    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.stocks_activity, menu);
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(EXTRA_UNIDADES, mUnidade);
        if (mDialogo != null) {
            outState.putBoolean(EXTRA_ADD_DIALOGO, mDialogo.isShowing());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDialogo != null) {
            mDialogo.dismiss();
            mDialogo = null;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_change_units) {
            if (mUnidade == UNIDADE_DOLARES) {
                mUnidade = UNIDADE_PERCENTUAL;
            } else {
                mUnidade = UNIDADE_DOLARES;
            }
            mAdapter.setUnidade(mUnidade);
            mAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mSemConexao.setVisibility(View.GONE);
        mSemCotacoes.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
        return new CursorLoader(this, CotacaoProvider.Cotacoes.CONTENT_URI,
                new String[]{CotacaoTable._ID, CotacaoTable.SYMBOL, CotacaoTable.BIDPRICE,
                        CotacaoTable.PERCENT_CHANGE, CotacaoTable.CHANGE, CotacaoTable.ISUP},
                CotacaoTable.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mProgressBar.setVisibility(View.GONE);
        mAdapter.swapCursor(data);

        if (mAdapter.getItemCount() == 0) {
            if (!isNetworkAvailable()) {
                mSemConexao.setVisibility(View.VISIBLE);
            } else {
                mSemCotacoes.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            mSemConexao.setVisibility(View.GONE);
            mSemCotacoes.setVisibility(View.GONE);
        }

        if (!isNetworkAvailable()) {
            Snackbar.make(mCoordinatorLayout, getString(R.string.offline),
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.tentar_novamente, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, MainActivity.this);
                }
            }).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.fab)
    public void showDialogAdicionandoCotacao() {
        if (isNetworkAvailable()) {
            mDialogo = new MaterialDialog.Builder(this).title(R.string.adicionar_sigla)
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .autoDismiss(true)
                    .positiveText(R.string.adicionar)
                    .negativeText(R.string.disagree)
                    .input(R.string.input_hint, R.string.input_pre_fill, false,
                            new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                    addCotacao(input.toString().trim().toUpperCase());
                                }
                            }).build();
            mDialogo.show();

        } else {
            Snackbar.make(mCoordinatorLayout, getString(R.string.sem_conexao_internet),
                    Snackbar.LENGTH_LONG).setAction(R.string.tentar_novamente, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialogAdicionandoCotacao();
                }
            }).show();
        }
    }

    @Override
    public void onItemClick(View v, int position) {
        Context context = v.getContext();
        Intent intent = new Intent(context, DetalheCotacaoActivity.class);
        intent.putExtra(CotacaoDetalheFragment.ARG_SYMBOL, mAdapter.getSymbol(position));
        context.startActivity(intent);
    }

    private void addCotacao(final String cotacao) {
        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                Cursor cursor = getContentResolver().query(CotacaoProvider.Cotacoes.CONTENT_URI,
                        new String[]{CotacaoTable.SYMBOL},
                        CotacaoTable.SYMBOL + "= ?",
                        new String[]{cotacao},
                        null);
                if (cursor != null) {
                    cursor.close();
                    return cursor.getCount() != 0;
                }
                return Boolean.FALSE;
            }

            @Override
            protected void onPostExecute(Boolean stockAlreadySaved) {
                if (stockAlreadySaved) {
                    Snackbar.make(mCoordinatorLayout, R.string.cotacao_adicionada,
                            Snackbar.LENGTH_LONG).show();
                } else {
                    Intent stockIntentService = new Intent(MainActivity.this,
                            CotacaoIntentService.class);
                    stockIntentService.putExtra(CotacaoIntentService.EXTRA_TAG, CotacaoIntentService.ACTION_ADD);
                    stockIntentService.putExtra(CotacaoIntentService.EXTRA_SYMBOL, cotacao);
                    startService(stockIntentService);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
