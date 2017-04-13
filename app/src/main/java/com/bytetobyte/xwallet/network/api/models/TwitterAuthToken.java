package com.bytetobyte.xwallet.network.api.models;

/**
 * Created by bruno on 12.04.17.
 */
public class TwitterAuthToken {
    private String token_type;
    private String access_token;

    public String getAccessToken() {
        return access_token;
    }

    public String getTokenType() {
        return token_type;
    }
}
