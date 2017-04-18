package com.tobiasandre.stockhawk.sync;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tobias Andre Eggers on 4/16/17.
 */

public class RespostaGetHistoricoCotacao {

    @SerializedName("query")
    private Results mResults;

    public List<Cotacao> getHistorico() {
        List<Cotacao> result = new ArrayList<>();
        if (mResults.getCotacao() != null) {
            List<Cotacao> cotacoes = mResults.getCotacao().getCotacoes();
            for (Cotacao cotacao : cotacoes) {
                result.add(cotacao);
            }
        }
        return result;
    }

    @SuppressWarnings("unused")
    public class Results {

        @SerializedName("count")
        private String mCount;

        @SerializedName("results")
        private Cotacoes mCotacao;

        public Cotacoes getCotacao() {
            return mCotacao;
        }
    }

    public class Cotacoes {

        @SerializedName("quote")
        private List<Cotacao> mCotacoes = new ArrayList<>();

        public List<Cotacao> getCotacoes() {
            return mCotacoes;
        }
    }

    public class Cotacao {

        @SerializedName("Symbol")
        private String mSymbol;

        @SerializedName("Date")
        private String mDate;

        @SerializedName("Low")
        private String mLow;

        @SerializedName("High")
        private String mHigh;

        @SerializedName("Open")
        private String mOpen;

        public String getSymbol() {
            return mSymbol;
        }

        public String getDate() {
            return mDate;
        }

        public String getOpen() {
            return mOpen;
        }
    }
}
