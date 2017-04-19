package com.bytetobyte.xwallet.network.api;

import android.os.AsyncTask;

import com.bytetobyte.xwallet.network.api.models.TwitterAuthToken;
import com.bytetobyte.xwallet.network.api.models.TwitterSearchResult;
import com.bytetobyte.xwallet.network.api.models.TwitterSearchStatuses;
import com.google.gson.Gson;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

import okio.GzipSource;
import okio.Okio;

/**
 * Created by bruno on 29.03.17.
 */
public class TwitterSearchApi extends AsyncTask<Void, Void, String>{
    private final static String TwitterSearchURL = "https://api.twitter.com/1.1/search/tweets.json";

    private final String _searchTerm;
    private final String _bearerToken;
    private final TwitterSearchCallback _callback;

    /**
     *
     * @param searchTerm
     */
    public TwitterSearchApi(String searchTerm, String bearerToken, TwitterSearchCallback callback) {
        this._searchTerm = searchTerm;
        this._bearerToken = bearerToken;
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

        HttpUrl httpUrl = HttpUrl.parse(TwitterSearchURL);
        httpUrl = httpUrl.newBuilder()
                .addQueryParameter("q", _searchTerm)
                .addQueryParameter("lang", "en")
                .addQueryParameter("result_type", "recent")
                .build();

        Request request = new Request.Builder()
                .url(httpUrl)
                .addHeader("Authorization", "Bearer " + _bearerToken)
                .addHeader("Accept-Encoding", "gzip")
                .build();

        System.out.println("Requesting : " + request.toString());

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

        System.out.println("Twitter Result : " + s);

        try {
            Gson gson = new Gson();
            TwitterSearchStatuses searchResult = gson.fromJson(s, TwitterSearchStatuses.class);
            _callback.onSearchResult(searchResult);
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

    /**
     *
     */
    public interface TwitterSearchCallback {
        public void onSearchResult(TwitterSearchStatuses result);
    }
}
