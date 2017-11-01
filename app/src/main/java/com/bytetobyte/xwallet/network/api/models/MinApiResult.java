package com.bytetobyte.xwallet.network.api.models;

import java.util.List;

/**
 * Created by bruno on 31/10/2017.
 */

public class MinApiResult {
    private String Response;
    private List<MinApiDataItem> Data;

    public String getResponse() {
        return Response;
    }

    public List<MinApiDataItem> getData() {
        return Data;
    }
}
