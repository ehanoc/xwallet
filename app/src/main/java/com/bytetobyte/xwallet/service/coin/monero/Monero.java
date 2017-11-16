package com.bytetobyte.xwallet.service.coin.monero;

import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.coin.CurrencyCoin;

import java.io.File;

/**
 * Created by bruno on 21.10.17.
 */

public class Monero implements CurrencyCoin<MoneroWalletManager> {

    public static final int WALLET_EXTRA_ID = 0x666;

    public static final String MONERO_DATA_DIR_NAME = "moneroDataDir";
    private final File _dataDir;
    private MoneroWalletManager _wallet;

    // ==========
    // Consts keys for extra options in txs
    // ==========
    public static final Integer KEY_TX_PAYMENT_ID = 0x5;
    public static final Integer KEY_TX_MIXINS = 0x6;
    public static final Integer KEY_TX_PRIORITY = 0xA;

    // possible mixin values
    public final static Integer Mixins[] = {4, 7, 12, 25};

    /**
     *
     * @param dataDir
     */
    public Monero(File dataDir) {
        this._dataDir = dataDir;
    }

    @Override
    public int getIconId() {
        return 0;
    }

    @Override
    public int getCoinId() {
        return CoinManagerFactory.MONERO;
    }

    @Override
    public String getCoinSymbol() {
        return "xmr";
    }

    @Override
    public File getDataDir() {
        return _dataDir;
    }

    @Override
    public void setWalletManager(MoneroWalletManager wallet) {
        this._wallet = wallet;
    }

    /**
     *
     * @return
     */
    @Override
    public MoneroWalletManager getWalletManager() {
        return _wallet;
    }
}
