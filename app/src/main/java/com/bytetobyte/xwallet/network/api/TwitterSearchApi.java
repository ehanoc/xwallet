package com.bytetobyte.xwallet.network.api;

import android.os.AsyncTask;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

/**
 * Created by bruno on 29.03.17.
 */
public class TwitterSearchApi extends AsyncTask<Void, Void, String>{
    private final static String TwitterSearchURL = "https://api.twitter.com/1.1/search/tweets.json?q=";

    private final String _searchTerm;

    /**
     *
     * @param searchTerm
     */
    public TwitterSearchApi(String searchTerm) {
        this._searchTerm = searchTerm;
    }

    /**
     *
     * @param params
     * @return
     */
    @Override
    protected String doInBackground(Void... params) {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(TwitterSearchURL + _searchTerm)
                .build();

        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     *
     * @param s
     */
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        System.out.println("Result : " + s);
    }
}
