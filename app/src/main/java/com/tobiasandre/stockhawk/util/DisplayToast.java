package com.tobiasandre.stockhawk.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by tobia on 20/04/2017.
 */

public class DisplayToast implements Runnable
{
    private final Context mContext;
    private final String mText;

    public DisplayToast(Context mContext, String mText)
    {
        this.mContext=mContext;
        this.mText=mText;
    }

    @Override
    public void run()
    {
        Toast.makeText(mContext,mText,Toast.LENGTH_SHORT);
    }
}