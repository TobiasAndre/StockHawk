package com.tobiasandre.stockhawk.data;

import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tobiasandre.stockhawk.MainActivity;
import com.tobiasandre.stockhawk.R;
import com.tobiasandre.stockhawk.stockwidget.ItemTouchHelperCallback;

/**
 * Created by Tobias Andre Eggers on 4/13/17.
 */

public class CotacaoCursorAdapter extends CursorRecyclerViewAdapter<CotacaoCursorAdapter.ViewHolder>
        implements ItemTouchHelperCallback.SwipeListener {

    private static Context mContext;
    private int mUnidade;

    public CotacaoCursorAdapter(Context context, Cursor cursor, int unidade) {
        super(cursor);
        mContext = context;
        mUnidade = unidade;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stock_list_content, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor) {
        viewHolder.mSymbol.setText(cursor.getString(cursor.getColumnIndex(CotacaoTable.SYMBOL)));
        viewHolder.mBidPrice.setText(cursor.getString(cursor.getColumnIndex(CotacaoTable.BIDPRICE)));
        if (cursor.getInt(cursor.getColumnIndex(CotacaoTable.ISUP)) == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                viewHolder.mChange.setBackground(
                        mContext.getResources().getDrawable(R.drawable.percent_change_green,
                                mContext.getTheme()));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                viewHolder.mChange.setBackground(
                        mContext.getResources().getDrawable(R.drawable.percent_change_red,
                                mContext.getTheme()));
            }
        }
        if (mUnidade == MainActivity.UNIDADE_PERCENTUAL) {
            viewHolder.mChange.setText(cursor.getString(cursor.getColumnIndex(CotacaoTable.PERCENT_CHANGE)));
        } else {
            viewHolder.mChange.setText(cursor.getString(cursor.getColumnIndex(CotacaoTable.CHANGE)));
        }
    }

    @Override
    public void onItemDismiss(int position) {
        String symbol = getSymbol(position);
        mContext.getContentResolver().delete(CotacaoProvider.Cotacoes.withSymbol(symbol), null, null);
        mContext.getContentResolver().delete(CotacaoProvider.HistoricoCotacao.CONTENT_URI,
                HistoricoCotacaoTable.SYMBOL + " = \"" + symbol + "\"", null);
        notifyItemRemoved(position);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    public String getSymbol(int position) {
        Cursor c = getCursor();
        c.moveToPosition(position);
        return c.getString(c.getColumnIndex(CotacaoTable.SYMBOL));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
            implements ItemTouchHelperCallback.ItemTouchHelperViewHolder, View.OnClickListener {
        public final TextView mSymbol;
        public final TextView mBidPrice;
        public final TextView mChange;

        public ViewHolder(View itemView) {
            super(itemView);
            mSymbol = (TextView) itemView.findViewById(R.id.stock_symbol);
            mBidPrice = (TextView) itemView.findViewById(R.id.bid_price);
            mChange = (TextView) itemView.findViewById(R.id.stock_change);
        }

        @Override
        public void onItemSelected() {
        }

        @Override
        public void onItemClear() {
        }

        @Override
        public void onClick(View v) {

        }
    }

    public void setUnidade(int unidade) {
        this.mUnidade = unidade;
    }

}
