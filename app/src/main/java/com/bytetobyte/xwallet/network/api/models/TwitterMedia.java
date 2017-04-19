package com.bytetobyte.xwallet.network.api.models;

/**
 * Created by bruno on 14.04.17.
 */
public class TwitterMedia {
    private String media_url;
    //
    private String media_url_https;
    //
    private String type;

    private String url;

    /**
     *
     * @return
     */
    public String getMediaUrl() {
        return media_url;
    }

    /**
     *
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @return
     */
    public String getMediaUrlHttps() {
        return media_url_https;
    }

    /**
     *
     * @return
     */
    public String getType() {
        return type;
    }
}
