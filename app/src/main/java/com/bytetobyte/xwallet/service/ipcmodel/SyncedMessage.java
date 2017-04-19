package com.bytetobyte.xwallet.service.ipcmodel;

import java.util.List;

/**
 * Created by bruno on 29.03.17.
 */
public class SyncedMessage {
    private String amount;
    private List<String> addresses;

    /**
     *
     * @param amount
     * @param addresses
     */
    public SyncedMessage(String amount, List<String> addresses) {
        this.amount = amount;
        this.addresses = addresses;
    }

    public String getAmount() {
        return amount;
    }

    public List<String> getAddresses() {
        return addresses;
    }
}
