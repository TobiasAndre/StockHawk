package com.tobiasandre.stockhawk;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.tobiasandre.stockhawk.data.CotacaoProvider;
import com.tobiasandre.stockhawk.data.CotacaoTable;
import com.tobiasandre.stockhawk.data.HistoricoCotacaoTable;
import com.tobiasandre.stockhawk.model.Cotacao;
import com.tobiasandre.stockhawk.sync.CotacoesService;
import com.tobiasandre.stockhawk.sync.RespostaGetCotacao;
import com.tobiasandre.stockhawk.sync.RespostaGetCotacoes;
import com.tobiasandre.stockhawk.sync.RespostaGetHistoricoCotacao;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Tobias Andre Eggers on 4/13/17.
 */

public class CotacaoTaskService extends GcmTaskService {

    private static String LOG_TAG = CotacaoTaskService.class.getSimpleName();
    private final static String INIT_QUOTES = "\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\"";
    public final static String TAG_PERIODIC = "periodic";

    private Context mContext;
    private StringBuilder mStoredSymbols = new StringBuilder();
    private boolean mIsUpdate;

    public CotacaoTaskService(Context context) {
        mContext = context;
    }

    @SuppressWarnings("unused")
    public CotacaoTaskService() {
    }

    @Override
    public int onRunTask(TaskParams params) {

        if (mContext == null) {
            return GcmNetworkManager.RESULT_FAILURE;
        }
        try {

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(CotacoesService.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            CotacoesService service = retrofit.create(CotacoesService.class);
            String query = "select * from yahoo.finance.quotes where symbol in ("
                    + buildUrl(params)
                    + ")";

            if (params.getTag().equals(CotacaoIntentService.ACTION_INIT)) {
                Call<RespostaGetCotacoes> call = service.getCotacoes(query);
                Response<RespostaGetCotacoes> response = call.execute();
                RespostaGetCotacoes responseGetStocks = response.body();
                saveQuotes2Database(responseGetStocks.getCotacoes());
            } else {
                Call<RespostaGetCotacao> call = service.getCotacao(query);
                Response<RespostaGetCotacao> response = call.execute();
                RespostaGetCotacao responseGetStock = response.body();
                saveQuotes2Database(responseGetStock.getCotacoes());
            }

            return GcmNetworkManager.RESULT_SUCCESS;

        } catch (IOException | RemoteException | OperationApplicationException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            return GcmNetworkManager.RESULT_FAILURE;
        }
    }

    private String buildUrl(TaskParams params) throws UnsupportedEncodingException {
        ContentResolver resolver = mContext.getContentResolver();
        if (params.getTag().equals(CotacaoIntentService.ACTION_INIT)
                || params.getTag().equals(TAG_PERIODIC)) {
            mIsUpdate = true;
            Cursor cursor = resolver.query(CotacaoProvider.Cotacoes.CONTENT_URI,
                    new String[]{"Distinct " + CotacaoTable.SYMBOL}, null,
                    null, null);

            if (cursor != null && cursor.getCount() == 0 || cursor == null) {
                return INIT_QUOTES;
            } else {
                DatabaseUtils.dumpCursor(cursor);
                cursor.moveToFirst();
                for (int i = 0; i < cursor.getCount(); i++) {
                    mStoredSymbols.append("\"");
                    mStoredSymbols.append(cursor.getString(
                            cursor.getColumnIndex(CotacaoTable.SYMBOL)));
                    mStoredSymbols.append("\",");
                    cursor.moveToNext();
                }
                mStoredSymbols.replace(mStoredSymbols.length() - 1, mStoredSymbols.length(), "");
                return mStoredSymbols.toString();
            }
        } else if (params.getTag().equals(CotacaoIntentService.ACTION_ADD)) {
            mIsUpdate = false;
            String stockInput = params.getExtras().getString(CotacaoIntentService.EXTRA_SYMBOL);
            return "\"" + stockInput + "\"";
        } else {
            throw new IllegalStateException("Acao não definida.");
        }
    }

    private void saveQuotes2Database(List<Cotacao> cotacoes) throws RemoteException, OperationApplicationException {
        ContentResolver resolver = mContext.getContentResolver();

        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        for (Cotacao cotacao : cotacoes) {

            batchOperations.add(CotacaoProvider.buildBatchOperation(cotacao));
        }

        if (mIsUpdate) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CotacaoTable.ISCURRENT, 0);
            resolver.update(CotacaoProvider.Cotacoes.CONTENT_URI, contentValues,
                    null, null);
        }

        resolver.applyBatch(CotacaoProvider.AUTHORITY, batchOperations);

        for (Cotacao cotacao : cotacoes) {
            try {
                loadHistoricalData(cotacao);
            } catch (IOException | RemoteException | OperationApplicationException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
            }
        }
    }

    private void loadHistoricalData(Cotacao cotacao) throws IOException, RemoteException,
            OperationApplicationException {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date currentDate = new Date();

        Calendar calEnd = Calendar.getInstance();
        calEnd.setTime(currentDate);
        calEnd.add(Calendar.DATE, 0);

        Calendar calStart = Calendar.getInstance();
        calStart.setTime(currentDate);
        calStart.add(Calendar.MONTH, -1);

        String startDate = dateFormat.format(calStart.getTime());
        String endDate = dateFormat.format(calEnd.getTime());

        String query = "select * from yahoo.finance.historicaldata where symbol=\"" +
                cotacao.getSymbol() +
                "\" and startDate=\"" + startDate + "\" and endDate=\"" + endDate + "\"";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CotacoesService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        CotacoesService service = retrofit.create(CotacoesService.class);
        Call<RespostaGetHistoricoCotacao> call = service.getHistoricoCotacao(query);
        Response<RespostaGetHistoricoCotacao> response;
        response = call.execute();
        RespostaGetHistoricoCotacao responseGetHistoricalData = response.body();
        if (responseGetHistoricalData != null) {
            saveQuoteHistoricalData2Database(responseGetHistoricalData.getHistorico());
        }
    }

    private void saveQuoteHistoricalData2Database(List<RespostaGetHistoricoCotacao.Cotacao> cotacoes)
            throws RemoteException, OperationApplicationException {
        ContentResolver resolver = mContext.getContentResolver();
        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        for (RespostaGetHistoricoCotacao.Cotacao cotacao : cotacoes) {

            resolver.delete(CotacaoProvider.HistoricoCotacao.CONTENT_URI,
                    HistoricoCotacaoTable.SYMBOL + " = \"" + cotacao.getSymbol() + "\"", null);

            batchOperations.add(CotacaoProvider.buildBatchOperation(cotacao));
        }

        resolver.applyBatch(CotacaoProvider.AUTHORITY, batchOperations);
    }
}
