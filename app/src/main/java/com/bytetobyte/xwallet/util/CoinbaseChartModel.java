package com.bytetobyte.xwallet.util;

import java.util.List;

/**
 * Created by bruno on 26.03.17.
 */
public class CoinbaseChartModel {
    private String status;
    private String name;
    private String unit;
    private String period;
    private String description;
    private List<ChartPoint> values;

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getUnit() {
        return unit;
    }

    public String getPeriod() {
        return period;
    }

    public String getDescription() {
        return description;
    }

    public List<ChartPoint> getValues() {
        return values;
    }
}
