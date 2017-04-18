package com.tobiasandre.stockhawk.sync;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Tobias Andre Eggers on 4/16/17.
 */

public interface CotacoesService {
    String BASE_URL = "https://query.yahooapis.com";

    @GET("/v1/public/yql?" +
            "format=json&diagnostics=true&" +
            "env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=")
    Call<RespostaGetCotacoes> getCotacoes(@Query("q") String query);

    @GET("/v1/public/yql?" +
            "format=json&diagnostics=true&" +
            "env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=")
    Call<RespostaGetCotacao> getCotacao(@Query("q") String query);

    @GET("/v1/public/yql?" +
            "format=json&diagnostics=true&" +
            "env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=")
    Call<RespostaGetHistoricoCotacao> getHistoricoCotacao(@Query("q") String query);
}
