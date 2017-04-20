package com.tobiasandre.stockhawk;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.gcm.TaskParams;
import com.tobiasandre.stockhawk.util.DisplayToast;

/**
 * Created by Tobias Andre Eggers on 4/13/17.
 */

public class CotacaoIntentService extends IntentService {

    public static final String EXTRA_TAG = "tag";
    public static final String EXTRA_SYMBOL = "symbol";

    public static final String ACTION_INIT = "init";
    public static final String ACTION_ADD = "add";

    public static Boolean SYMBOL_NOT_FOUND=false;



    public CotacaoIntentService() {
        super(CotacaoIntentService.class.getName());

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle args = new Bundle();
        if (intent.getStringExtra(EXTRA_TAG).equals(ACTION_ADD)) {
            args.putString(EXTRA_SYMBOL, intent.getStringExtra(EXTRA_SYMBOL));
        }

        CotacaoTaskService stockTaskService = new CotacaoTaskService(this);
        stockTaskService.onRunTask(new TaskParams(intent.getStringExtra(EXTRA_TAG), args));




    }
}
