package com.tobiasandre.stockhawk.data;

import android.content.ContentProviderOperation;
import android.net.Uri;

import com.tobiasandre.stockhawk.model.Cotacao;
import com.tobiasandre.stockhawk.sync.RespostaGetHistoricoCotacao;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

import java.util.Locale;

/**
 * Created by Tobias Andre Eggers on 4/10/17.
 */

@ContentProvider(authority = CotacaoProvider.AUTHORITY, database = CotacaoDataBase.class)
public class CotacaoProvider {

    public static final String AUTHORITY = "com.tobiasandre.stockhawk.data.CotacaoProvider";

    static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    interface Path {
        String COTACOES = "quotes";
        String HISTORICO_COTACOES = "quotes_historical_data";
    }

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    @TableEndpoint(table = CotacaoDataBase.HISTORICO_COTACOES)
    public static class HistoricoCotacao {
        @ContentUri(
                path = Path.HISTORICO_COTACOES,
                type = "vnd.android.cursor.dir/quote_historical_data"
        )
        public static final Uri CONTENT_URI = buildUri(Path.HISTORICO_COTACOES);
    }

    @TableEndpoint(table = CotacaoDataBase.COTACOES)
    public static class Cotacoes {
        @ContentUri(
                path = Path.COTACOES,
                type = "vnd.android.cursor.dir/quote"
        )
        public static final Uri CONTENT_URI = buildUri(Path.COTACOES);

        @InexactContentUri(
                name = "QUOTE_ID",
                path = Path.COTACOES + "/*",
                type = "vnd.android.cursor.item/quote",
                whereColumn = CotacaoTable.SYMBOL,
                pathSegment = 1
        )
        public static Uri withSymbol(String symbol) {
            return buildUri(Path.COTACOES, symbol);
        }
    }

    public static ContentProviderOperation buildBatchOperation(RespostaGetHistoricoCotacao.Cotacao cotacao) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                CotacaoProvider.HistoricoCotacao.CONTENT_URI);
        builder.withValue(HistoricoCotacaoTable.SYMBOL, cotacao.getSymbol());
        builder.withValue(HistoricoCotacaoTable.BIDPRICE, cotacao.getOpen());
        builder.withValue(HistoricoCotacaoTable.DATE, cotacao.getDate());
        return builder.build();
    }

    public static ContentProviderOperation buildBatchOperation(Cotacao cotacao) {

        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                CotacaoProvider.Cotacoes.CONTENT_URI);
        String change = cotacao.getChange();
        builder.withValue(CotacaoTable.SYMBOL, cotacao.getSymbol());
        builder.withValue(CotacaoTable.BIDPRICE, truncateBidPrice(cotacao.getBid()));
        builder.withValue(CotacaoTable.PERCENT_CHANGE, truncateChange(
                cotacao.getChangeInPercent(), true));
        builder.withValue(CotacaoTable.CHANGE, truncateChange(change, false));
        builder.withValue(CotacaoTable.ISCURRENT, 1);
        if (change.charAt(0) == '-') {
            builder.withValue(CotacaoTable.ISUP, 0);
        } else {
            builder.withValue(CotacaoTable.ISUP, 1);
        }
        builder.withValue(CotacaoTable.NAME, cotacao.getName());
        return builder.build();
    }

    private static String truncateBidPrice(String bidPrice) {
        bidPrice = String.format(Locale.US, "%.2f", Float.parseFloat(bidPrice));
        return bidPrice;
    }

    private static String truncateChange(String change, boolean isPercentChange) {
        String weight = change.substring(0, 1);
        String ampersand = "";
        if (isPercentChange) {
            ampersand = change.substring(change.length() - 1, change.length());
            change = change.substring(0, change.length() - 1);
        }
        change = change.substring(1, change.length());
        double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
        change = String.format(Locale.US, "%.2f", round);
        StringBuilder changeBuffer = new StringBuilder(change);
        changeBuffer.insert(0, weight);
        changeBuffer.append(ampersand);
        change = changeBuffer.toString();
        return change;
    }

}
