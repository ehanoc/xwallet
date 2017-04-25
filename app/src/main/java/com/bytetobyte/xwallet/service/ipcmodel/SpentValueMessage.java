package com.bytetobyte.xwallet.service.ipcmodel;

/**
 * Created by bruno on 25.03.17.
 */
public class SpentValueMessage {

    private String _address;
    private String _amount;
    private String _txFee;

    /**
     *
     * @param address
     * @param amount
     */
    public SpentValueMessage(String address, String amount) {
        this._address = address;
        this._amount = amount;
        this._txFee = null;
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
