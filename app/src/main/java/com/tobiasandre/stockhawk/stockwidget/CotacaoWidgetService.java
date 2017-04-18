package com.tobiasandre.stockhawk.stockwidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by Tobias Andre Eggers on 4/17/17.
 */

public class CotacaoWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new CotacaoWidgetFactory(getApplicationContext(), intent);
    }
}