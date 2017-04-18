package com.tobiasandre.stockhawk.stockwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.tobiasandre.stockhawk.R;
import com.tobiasandre.stockhawk.data.CotacaoProvider;
import com.tobiasandre.stockhawk.data.CotacaoTable;

/**
 * Created by Tobias Andre Eggers on 4/17/17.
 */

public class CotacaoWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private Cursor mCursor;
    private Context mContext;
    int mWidgetId;

    public CotacaoWidgetFactory(Context context, Intent intent) {
        mContext = context;
        mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.cotacao_widget_list_content);
        if (mCursor.moveToPosition(position)) {
            rv.setTextViewText(R.id.stock_symbol,
                    mCursor.getString(mCursor.getColumnIndex(CotacaoTable.SYMBOL)));
            rv.setTextViewText(R.id.bid_price,
                    mCursor.getString(mCursor.getColumnIndex(CotacaoTable.BIDPRICE)));
            rv.setTextViewText(R.id.stock_change,
                    mCursor.getString(mCursor.getColumnIndex(CotacaoTable.CHANGE)));
        }
        return rv;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataSetChanged() {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = mContext.getContentResolver().query(CotacaoProvider.Cotacoes.CONTENT_URI,
                new String[]{CotacaoTable._ID, CotacaoTable.SYMBOL, CotacaoTable.BIDPRICE,
                        CotacaoTable.PERCENT_CHANGE, CotacaoTable.CHANGE, CotacaoTable.ISUP},
                CotacaoTable.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onDestroy() {
        if (mCursor != null) {
            mCursor.close();
        }
    }

}
