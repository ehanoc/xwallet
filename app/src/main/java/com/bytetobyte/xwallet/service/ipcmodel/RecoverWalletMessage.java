package com.bytetobyte.xwallet.service.ipcmodel;

import java.util.Date;

/**
 * Created by bruno on 26.04.17.
 */
public class RecoverWalletMessage {

    private String _seed;
    private Date _date;

    /**
     *
     * @param seed
     * @param date
     */
    public RecoverWalletMessage(String seed, Date date) {
        this._seed = seed;
        this._date = date;
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
