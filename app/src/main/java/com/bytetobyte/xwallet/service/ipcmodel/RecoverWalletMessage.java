package com.bytetobyte.xwallet.service.ipcmodel;

import java.util.Date;

/**
 * Created by bruno on 26.04.17.
 */
public class RecoverWalletMessage {

    private boolean _isViewOnly;
    private String _input;
    private Date _date;
    private long _blockHeight;

    /**
     *
     * @param input
     * @param date
     */
    public RecoverWalletMessage(String input, Date date, long blockHeight, boolean isViewOnly) {
        this._input = input;
        this._date = date;
        this._blockHeight = blockHeight;
        this._isViewOnly = isViewOnly;
    }

    /**
     *
     * @return
     */
    public boolean isViewOnly() {
        return _isViewOnly;
    }

    /**
     *
     * @return
     */
    public long getBlockHeight() {
        return _blockHeight;
    }

    /**
     *
     * @return
     */
    public String getInput() {
        return _input;
    }

    /**
     *
     * @return
     */
    public Date getDate() {
        return _date;
    }
}
