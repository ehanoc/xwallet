package com.bytetobyte.xwallet.network.api;

import android.os.AsyncTask;

import com.bytetobyte.xwallet.network.api.models.MinApiResult;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.google.gson.Gson;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

import okio.GzipSource;
import okio.Okio;

/**
 * Created by bruno on 31/10/2017.
 */

public class PriceRequestAPI extends AsyncTask<Void, Void, String> {

    public static String MONERO_PRICE_REQUEST = "https://min-api.cryptocompare.com/data/histominute?fsym=XMR&tsym=USD&limit=360&aggregate=3&e=CCCAGG";
    public static String BITCOIN_PRICE_REQUEST = "https://min-api.cryptocompare.com/data/histominute?fsym=BTC&tsym=USD&limit=360&aggregate=3&e=CCCAGG";

    /**
     *
     */
    private final PriceAPICallback _callback;
    private final String _url;


    public PriceRequestAPI(String url, PriceAPICallback callback) {
        this._callback = callback;
        this._url = url;
    }


    @Override
    protected String doInBackground(Void... voids) {
        OkHttpClient client = new OkHttpClient();

        HttpUrl httpUrl = HttpUrl.parse(_url);

        Request request = new Request.Builder()
                .url(httpUrl)
                .build();

        String content = null;
        try {
            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();

            if (isZipped(response)) {
                content = unzip(body);
            } else {
                content = body.string();
            }

            body.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return content;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        try {
            Gson gson = new Gson();
            MinApiResult resultItem = gson.fromJson(s, MinApiResult.class);
            _callback.onPriceResult(resultItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isZipped(Response response) {
        return "gzip".equalsIgnoreCase(response.header("Content-Encoding"));
    }

    /**
     *
     * @param body
     * @return
     */
    private String unzip(ResponseBody body) {
        try {
            GzipSource responseBody = new GzipSource(body.source());
            return Okio.buffer(responseBody).readUtf8();
        } catch (IOException e) {
            return null;
        }
    }

    /**
     *
     * @param coinId
     * @return
     */
    public static String GetCoinUrl(int coinId) {
        if (CoinManagerFactory.MONERO == coinId)
            return MONERO_PRICE_REQUEST;

        return BITCOIN_PRICE_REQUEST;
    }

    public interface PriceAPICallback {
       void onPriceResult(MinApiResult result);
    }
}
