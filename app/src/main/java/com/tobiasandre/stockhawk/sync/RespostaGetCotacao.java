package com.tobiasandre.stockhawk.sync;

import com.google.gson.annotations.SerializedName;
import com.tobiasandre.stockhawk.model.Cotacao;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tobias Andre Eggers on 4/16/17.
 */

public class RespostaGetCotacao {

    @SerializedName("query")
    private Result mResult;

    public List<Cotacao> getCotacoes() {
        List<Cotacao> result = new ArrayList<>();
        if (mResult != null && mResult.getCota() != null) {
            Cotacao cotacoes = mResult.getCota().getCotacao();
            if (cotacoes.getBid() != null && cotacoes.getChangeInPercent() != null
                    && cotacoes.getChange() != null) {
                result.add(cotacoes);
            }
        }
        return result;
    }

    public class Result {

        @SerializedName("count")
        private int mCount;

        @SerializedName("results")
        private Cota mCota;

        public Cota getCota() {
            return mCota;
        }
    }

    public class Cota {

        @SerializedName("quote")
        private Cotacao mCotacao;

        public Cotacao getCotacao() {
            return mCotacao;
        }
    }

}
