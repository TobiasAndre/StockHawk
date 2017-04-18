package com.tobiasandre.stockhawk;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

import com.tobiasandre.stockhawk.data.CotacaoProvider;
import com.tobiasandre.stockhawk.data.CotacaoTable;
import com.tobiasandre.stockhawk.data.HistoricoCotacaoTable;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by Tobias Andre Eggers on 4/14/17.
 */

public class CotacaoDetalheFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        TabHost.OnTabChangeListener {

    @SuppressWarnings("unused")
    public static String LOG_TAG = CotacaoDetalheFragment.class.getSimpleName();
    public static final String ARG_SYMBOL = "ARG_SYMBOL";
    public static final String CURRENT_TAB_EXTRA = "CURRENT_TAB_EXTRA";
    private static final int CURSOR_LOADER_ID = 1;
    private static final int CURSOR_LOADER_ID_FOR_LINE_CHART = 2;

    private String mSigla;
    private String mTabSelecionada;

    @Bind(R.id.stock_name)
    TextView mNameView;
    @Bind(R.id.stock_symbol)
    TextView mSiglaView;
    @Bind(R.id.stock_bidprice)
    TextView mEbitdaView;
    @Bind(android.R.id.tabhost)
    TabHost mTabHost;
    @Bind(R.id.stock_chart)
    LineChartView mGrafico;
    @Bind(R.id.stock_change)
    TextView mChange;
    @Bind(android.R.id.tabcontent)
    View mConteudoTab;

    public CotacaoDetalheFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_SYMBOL)) {
            mSigla = getArguments().getString(ARG_SYMBOL);
        }

        if (getActionBar() != null) {
            getActionBar().setElevation(0);
            if (getActivity() instanceof DetalheCotacaoActivity) {
                getActionBar().setTitle("");
            }
        }

        if (savedInstanceState == null) {
            mTabSelecionada = getString(R.string.stock_detail_tab1);
        } else {
            mTabSelecionada = savedInstanceState.getString(CURRENT_TAB_EXTRA);
        }

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        getLoaderManager().initLoader(CURSOR_LOADER_ID_FOR_LINE_CHART, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.detalhe_cotacao, container, false);
        ButterKnife.bind(this, rootView);
        setupTabs();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_TAB_EXTRA, mTabSelecionada);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == CURSOR_LOADER_ID) {
            return new CursorLoader(getContext(), CotacaoProvider.Cotacoes.CONTENT_URI,
                    new String[]{CotacaoTable._ID, CotacaoTable.SYMBOL, CotacaoTable.BIDPRICE,
                            CotacaoTable.PERCENT_CHANGE, CotacaoTable.CHANGE, CotacaoTable.ISUP,
                            CotacaoTable.NAME},
                    CotacaoTable.SYMBOL + " = \"" + mSigla + "\"",
                    null, null);
        } else if (id == CURSOR_LOADER_ID_FOR_LINE_CHART) {

            String sortOrder = CotacaoTable._ID + " ASC LIMIT 5";
            if (mTabSelecionada.equals(getString(R.string.stock_detail_tab2))) {
                sortOrder = CotacaoTable._ID + " ASC LIMIT 14";
            } else if (mTabSelecionada.equals(getString(R.string.stock_detail_tab3))) {
                sortOrder = CotacaoTable._ID + " ASC";
            }

            return new CursorLoader(getContext(), CotacaoProvider.HistoricoCotacao.CONTENT_URI,
                    new String[]{HistoricoCotacaoTable._ID, HistoricoCotacaoTable.SYMBOL,
                            HistoricoCotacaoTable.BIDPRICE, HistoricoCotacaoTable.DATE},
                    HistoricoCotacaoTable.SYMBOL + " = \"" + mSigla + "\"",
                    null, sortOrder);
        } else {
            throw new IllegalStateException();
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == CURSOR_LOADER_ID && data != null && data.moveToFirst()) {

            String symbol = data.getString(data.getColumnIndex(CotacaoTable.SYMBOL));
            mSiglaView.setText(getString(R.string.detalhe_cotacao_tab_header, symbol));

            String ebitda = data.getString(data.getColumnIndex(CotacaoTable.BIDPRICE));
            mEbitdaView.setText(ebitda);

            String name = data.getString(data.getColumnIndex(CotacaoTable.NAME));
            mNameView.setText(name);

            String change = data.getString(data.getColumnIndex(CotacaoTable.CHANGE));
            String percentChange = data.getString(data.getColumnIndex(CotacaoTable.PERCENT_CHANGE));
            String mixedChange = change + " (" + percentChange + ")";
            mChange.setText(mixedChange);

        } else if (loader.getId() == CURSOR_LOADER_ID_FOR_LINE_CHART && data != null &&
                data.moveToFirst()) {
            atualizaGrafico(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onTabChanged(String tabId) {
        mTabSelecionada = tabId;
        getLoaderManager().restartLoader(CURSOR_LOADER_ID_FOR_LINE_CHART, null, this);
    }

    @Nullable
    private ActionBar getActionBar() {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            return activity.getSupportActionBar();
        }
        return null;
    }

    private void setupTabs() {
        mTabHost.setup();

        TabHost.TabSpec tabSpec;
        tabSpec = mTabHost.newTabSpec(getString(R.string.stock_detail_tab1));
        tabSpec.setIndicator(getString(R.string.stock_detail_tab1));
        tabSpec.setContent(android.R.id.tabcontent);
        mTabHost.addTab(tabSpec);

        tabSpec = mTabHost.newTabSpec(getString(R.string.stock_detail_tab2));
        tabSpec.setIndicator(getString(R.string.stock_detail_tab2));
        tabSpec.setContent(android.R.id.tabcontent);
        mTabHost.addTab(tabSpec);

        tabSpec = mTabHost.newTabSpec(getString(R.string.stock_detail_tab3));
        tabSpec.setIndicator(getString(R.string.stock_detail_tab3));
        tabSpec.setContent(android.R.id.tabcontent);
        mTabHost.addTab(tabSpec);

        mTabHost.setOnTabChangedListener(this);

        if (mTabSelecionada.equals(getString(R.string.stock_detail_tab2))) {
            mTabHost.setCurrentTab(1);
        } else if (mTabSelecionada.equals(getString(R.string.stock_detail_tab3))) {
            mTabHost.setCurrentTab(2);
        } else {
            mTabHost.setCurrentTab(0);
        }
    }

    private void atualizaGrafico(Cursor data) {

        List<AxisValue> axisValuesX = new ArrayList<>();
        List<PointValue> pointValues = new ArrayList<>();

        int counter = -1;
        do {
            counter++;

            String date = data.getString(data.getColumnIndex(
                    HistoricoCotacaoTable.DATE));
            String bidPrice = data.getString(data.getColumnIndex(
                    HistoricoCotacaoTable.BIDPRICE));

            int x = data.getCount() - 1 - counter;

            PointValue pointValue = new PointValue(x, Float.valueOf(bidPrice));
            pointValue.setLabel(date);
            pointValues.add(pointValue);

            if (counter != 0 && counter % (data.getCount() / 3) == 0) {
                AxisValue axisValueX = new AxisValue(x);
                axisValueX.setLabel(date);
                axisValuesX.add(axisValueX);
            }

        } while (data.moveToNext());

        Line linha = new Line(pointValues).setColor(Color.WHITE).setCubic(false);
        List<Line> linhas = new ArrayList<>();
        linhas.add(linha);
        LineChartData linhaDadosGrafico = new LineChartData();
        linhaDadosGrafico.setLines(linhas);

        Axis axisX = new Axis(axisValuesX);
        axisX.setHasLines(true);
        axisX.setMaxLabelChars(4);
        linhaDadosGrafico.setAxisXBottom(axisX);

        Axis axisY = new Axis();
        axisY.setAutoGenerated(true);
        axisY.setHasLines(true);
        axisY.setMaxLabelChars(4);
        linhaDadosGrafico.setAxisYLeft(axisY);


        mGrafico.setInteractive(false);
        mGrafico.setLineChartData(linhaDadosGrafico);

        mGrafico.setVisibility(View.VISIBLE);
        mConteudoTab.setVisibility(View.VISIBLE);
    }
}
