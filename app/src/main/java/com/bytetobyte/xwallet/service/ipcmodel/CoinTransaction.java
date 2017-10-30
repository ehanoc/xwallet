package com.bytetobyte.xwallet.service.ipcmodel;

import java.util.Date;

/**
 * Created by bruno on 19.04.17.
 */
public class CoinTransaction implements Comparable<CoinTransaction> {

    private int _coinId;
    private String _txFee;
    private String _txHash;
    private String _txAmount;
    private String _confirmations;
    private Date _txUpdate;

    /**
     *
     * @param fee
     * @param hash
     * @param amount
     */
    public CoinTransaction(int coinId, String fee, String hash, String amount, String confirmations, Date updateTime) {
        this._coinId = coinId;
        this._txFee = fee;
        this._txHash = hash;
        this._txAmount = amount;
        this._txUpdate = updateTime;
        this._confirmations = confirmations;
    }

    public Date getTxUpdate() {
        return _txUpdate;
    }

    /**
     *
     * @return
     */
    public String getConfirmations() {
        return _confirmations;
    }

    /**
     *
     * @return
     */
    public String getTxFee() {
        return _txFee;
    }


    /**
     *
     * @return
     */
    public String getTxAmount() {
        return _txAmount;
    }

    /**
     *
     * @return
     */
    public String getTxHash() {
        return _txHash;
    }

    public int getCoinId() {
        return _coinId;
    }
    /**
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(CoinTransaction o) {
        return _txUpdate.compareTo(o.getTxUpdate());
    }
}
