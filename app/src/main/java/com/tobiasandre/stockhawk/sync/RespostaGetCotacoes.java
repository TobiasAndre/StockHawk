package com.tobiasandre.stockhawk.sync;

import com.google.gson.annotations.SerializedName;
import com.tobiasandre.stockhawk.model.Cotacao;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tobias Andre Eggers on 4/16/17.
 */

public class RespostaGetCotacoes {
    @SerializedName("query")
    private Results mResults;

    public List<Cotacao> getCotacoes() {
        List<Cotacao> result = new ArrayList<>();
        List<Cotacao> cotacoes = mResults.getCotacao().getCotacoes();
        for (Cotacao cotacao : cotacoes) {
            if (cotacao.getBid() != null && cotacao.getChangeInPercent() != null
                    && cotacao.getChange() != null) {
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
        private Cotacoes mCotacoes;

        public Cotacoes getCotacao() {
            return mCotacoes;
        }
    }

    public class Cotacoes {

        @SerializedName("quote")
        private List<Cotacao> mCotacoes = new ArrayList<>();

        public List<Cotacao> getCotacoes() {
            return mCotacoes;
        }
    }
}
