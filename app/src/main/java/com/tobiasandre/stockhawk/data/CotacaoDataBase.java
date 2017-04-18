package com.tobiasandre.stockhawk.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by Tobias Andre Eggers on 4/10/17.
 */

@Database(version = CotacaoDataBase.VERSION)
public class CotacaoDataBase {

    private CotacaoDataBase() {
    }

    public static final int VERSION = 9;

    @Table(CotacaoTable.class)
    public static final String COTACOES = "quotes";

    @Table(HistoricoCotacaoTable.class)
    public static final String HISTORICO_COTACOES = "quotes_historical_data";

}
