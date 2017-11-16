package com.bytetobyte.xwallet.service.ipcmodel;

import java.util.Map;

/**
 * Created by bruno on 25.03.17.
 */
public class SpentValueMessage {

    private String _address;
    private String _amount;
    private String _txFee;

    private Map<Integer, Object> _extraOptions;

    /**
     *
     * @param address
     * @param amount
     * @param txFee
     * @param extraOptions
     */
    public SpentValueMessage(String address, String amount, String txFee, Map<Integer, Object> extraOptions) {
        this._address = address;
        this._amount = amount;
        this._txFee = txFee;
        this._extraOptions = extraOptions;
    }

    /**
     *
     * @return
     */
    public Map<Integer, Object> getExtraOptions() {
        return _extraOptions;
    }

    /**
     *
     * @param txFee
     */
    public void setTxFee(String txFee) {
        this._txFee = txFee;
    }

    /**
     *
     * @param amount
     */
    public void setAmount(String amount) {
        this._amount = amount;
    }

    /**
     *
     * @return
     */
    public String getTxFee() {
        return _txFee;
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
    public String getAmount() {
        return _amount;
    }
}
