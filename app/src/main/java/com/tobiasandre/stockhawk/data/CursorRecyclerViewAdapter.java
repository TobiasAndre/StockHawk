package com.tobiasandre.stockhawk.data;

import android.database.Cursor;
import android.database.DataSetObserver;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Tobias Andre Eggers on 4/13/17.
 */
public abstract class CursorRecyclerViewAdapter<VH extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    
    private Cursor mCursor;
    private boolean mDataIsValid;
    private int mRowIdColumn;
    private DataSetObserver mDataSetObserver;

    public CursorRecyclerViewAdapter(Cursor cursor) {
        mCursor = cursor;
        mDataIsValid = cursor != null;
        mRowIdColumn = mDataIsValid ? mCursor.getColumnIndex("_id") : -1;
        mDataSetObserver = new NotifyingDataSetObserver();
        if (mDataIsValid) {
            mCursor.registerDataSetObserver(mDataSetObserver);
        }
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        }
        return 0;
    }

    @Override
    public long getItemId(int position) {
        if (mDataIsValid && mCursor != null && mCursor.moveToPosition(position)) {
            return mCursor.getLong(mRowIdColumn);
        }
        return 0;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    public abstract void onBindViewHolder(VH viewHolder, Cursor cursor);

    @Override
    public void onBindViewHolder(VH viewHolder, int position) {
        if (!mDataIsValid) {
            throw new IllegalStateException("Isto só sera possivel quando o cursor estiver em estado válido.");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Não é possivel mudar a posição do cursor para : " + position);
        }

        onBindViewHolder(viewHolder, mCursor);
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        final Cursor oldCursor = mCursor;
        if (oldCursor != null && mDataSetObserver != null) {
            oldCursor.unregisterDataSetObserver(mDataSetObserver);
        }
        mCursor = newCursor;
        if (mCursor != null) {
            if (mDataSetObserver != null) {
                mCursor.registerDataSetObserver(mDataSetObserver);
            }
            mRowIdColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataIsValid = true;
            notifyDataSetChanged();
        } else {
            mRowIdColumn = -1;
            mDataIsValid = false;
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    private class NotifyingDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            mDataIsValid = true;
            notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            mDataIsValid = false;
            notifyDataSetChanged();
        }
    }
}
