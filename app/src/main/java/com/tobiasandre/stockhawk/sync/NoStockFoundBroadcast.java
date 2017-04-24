package com.tobiasandre.stockhawk.sync;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.tobiasandre.stockhawk.R;

/**
 * Created by tobia on 20/04/2017.
 */

public class NoStockFoundBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, context.getString(R.string.cotacao_inexistente), Toast.LENGTH_SHORT).show();
    }
}
