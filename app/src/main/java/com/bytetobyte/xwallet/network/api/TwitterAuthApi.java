package com.bytetobyte.xwallet.network.api;

import android.os.AsyncTask;
import android.util.Base64;

import com.bytetobyte.xwallet.network.api.models.TwitterAuthToken;
import com.google.gson.Gson;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

import okio.GzipSource;
import okio.Okio;

/**
 * Created by bruno on 29.03.17.
 */
public class TwitterAuthApi extends AsyncTask <Void, Void, String>{

    private final static String TWITTER_AUTH_URL = "https://api.twitter.com/oauth2/token";

    private final String _key;
    private final String _secret;
    private final AuthCallback _authCallback;

    /**
     *
     * @param key
     * @param secret
     */
    public TwitterAuthApi(String key, String secret, AuthCallback callback) {
        this._key = key;
        this._secret = secret;
        this._authCallback = callback;
    }

    /**
     *
     * @param params
     * @return
     */
    @Override
    protected String doInBackground(Void... params) {

        OkHttpClient client = new OkHttpClient();

        String cred = Credentials.basic(_key, _secret);

        FormEncodingBuilder formBody = new FormEncodingBuilder();
        formBody.add("grant_type", "client_credentials");

        Request request = new Request.Builder()
                .url(TWITTER_AUTH_URL)
                .post(formBody.build())
                .addHeader("Authorization", cred)
                .addHeader("ContentType", "application/x-www-form-urlencoded;charset=UTF-8")
                .addHeader("Accept-Encoding", "gzip")
                .build();

        try {
            Response response = client.newCall(request).execute();

            if (isZipped(response)) {
                return unzip(response.body());
            } else {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
     * @param s
     */
    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        try {
            Gson gson = new Gson();
            TwitterAuthToken authResponse = gson.fromJson(s, TwitterAuthToken.class);
            _authCallback.onTwitterAuth(authResponse);
        } catch (Exception e) { e.printStackTrace(); }
    }

    /**
     *
     */
    public interface AuthCallback {
        void onTwitterAuth(TwitterAuthToken response);
    }
}
