package com.bytetobyte.xwallet.network.api;

import android.os.AsyncTask;

import com.bytetobyte.xwallet.network.api.models.CexCharItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okio.GzipSource;
import okio.Okio;

/**
 * Created by bruno on 07.05.17.
 */
public class CexChartAPI extends AsyncTask<Void, Void, String> {

    private final static String COIN_DESK_API_URL = "https://cex.io/api/price_stats/BTC/USD";
    private final CexChartCallback _callback;

    /**
     *
     * @param callback
     */
    public CexChartAPI(CexChartCallback callback) {
        this._callback = callback;
    }

    /**
     *
     * @param params
     * @return
     */
    @Override
    protected String doInBackground(Void... params) {
        OkHttpClient client = new OkHttpClient();

        HttpUrl httpUrl = HttpUrl.parse(COIN_DESK_API_URL);
        //System.out.println("Requesting : " + httpUrl.toString());

        FormEncodingBuilder formBody = new FormEncodingBuilder();
        formBody.add("lastHours", "24");
        formBody.add("maxRespArrSize", "24");

        Request request = new Request.Builder()
                .url(httpUrl)
                .post(formBody.build())
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

    /**
     *
     * @param s
     */
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        //System.out.println("CexChartApi result : " + s);

        try {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<CexCharItem>>(){}.getType();
            List<CexCharItem> searchResult = gson.fromJson(s, listType);
            _callback.onChartResult(searchResult);
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     *
     * @param response
     * @return
     */
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

    public interface CexChartCallback {
        void onChartResult(List<CexCharItem> result);
    }
}
