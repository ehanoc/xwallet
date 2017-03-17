package com.bytetobyte.xwallet.service.ipc;

/**
 * Created by bruno on 25.03.17.
 */
public class SpentValueMessage {

    private String _address;
    private long _amount;

    /**
     *
     * @param address
     * @param amount
     */
    public SpentValueMessage(String address, long amount) {
        this._address = address;
        this._amount = amount;
    }

    /**
     * '
     * @return
     */
    public String getAddress() {
        return _address;
    }

    /**
     *
     * @return
     */
    public long getAmount() {
        return _amount;
    }


}
