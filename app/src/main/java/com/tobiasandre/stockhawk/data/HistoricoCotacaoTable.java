package com.tobiasandre.stockhawk.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by Tobias Andre Eggers on 4/10/17.
 */

public class HistoricoCotacaoTable {
    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";
    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String SYMBOL = "symbol";
    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String DATE = "date";
    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String BIDPRICE = "bid_price";
}
