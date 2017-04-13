package com.bytetobyte.xwallet.network.api.models;

/**
 * Created by bruno on 12.04.17.
 */
public class TwitterSearchResult {
    private String created_at;
    private String id_str;
    private String text;
    private boolean truncated;

    public String getCreatedAt() {
        return created_at;
    }

    public String getIdStr() {
        return id_str;
    }

    public String getText() {
        return text;
    }

    public boolean isTruncated() {
        return truncated;
    }
}
