package com.bytetobyte.xwallet.service.ipcmodel;

import java.util.Date;

/**
 * Created by bruno on 26.04.17.
 */
public class RecoverWalletMessage {

    private String _seed;
    private Date _date;
    private long _blockHeight;

    /**
     *
     * @param seed
     * @param date
     */
    public RecoverWalletMessage(String seed, Date date, long blockHeight) {
        this._seed = seed;
        this._date = date;
        this._blockHeight = blockHeight;
    }

    public long getBlockHeight() {
        return _blockHeight;
    }

    /**
     *
     * @return
     */
    public String getSeed() {
        return _seed;
    }

    /**
     *
     * @return
     */
    public Date getDate() {
        return _date;
    }
}
