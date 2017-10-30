package com.bytetobyte.xwallet.service.ipcmodel;

import com.bytetobyte.xwallet.service.coin.CurrencyCoin;

import java.util.Date;

/**
 * Created by bruno on 10.04.17.
 */
public class BlockDownloaded {

    private final int _coin;
    private final double _pct;
    private final long _blocksLeft;
    private final Date _lastBlockDate;

    /**
     *
     * @param coin
     * @param pct
     * @param blocksSoFar
     * @param date
     */
    public BlockDownloaded(CurrencyCoin coin, double pct, long blocksSoFar, Date date) {
        this._coin = coin.getCoinId();
        this._pct = pct;
        this._blocksLeft = blocksSoFar;
        this._lastBlockDate = date;
    }

    public int getCoin() {
        return _coin;
    }

    public double getPct() {
        return _pct;
    }

    public long getBlocksLeft() {
        return _blocksLeft;
    }

    public Date getLastBlockDate() {
        return _lastBlockDate;
    }
}
