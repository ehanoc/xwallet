package com.bytetobyte.xwallet.service.coin.bitcoin;

import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.coin.CurrencyCoin;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.io.File;

/**
 * Created by bruno on 22.03.17.
 */
public class Bitcoin implements CurrencyCoin<Wallet> {

    /**
     *
     */
    private File _dataDir = null;
    public static final String BITCOIN_DATA_DIR_NAME = "bitcoinDataDir";
    private Wallet _wallet;
    private Class<Wallet> _walletClassType;

    /**
     *
     * @param dataDir
     */
    public Bitcoin(File dataDir) {
       _dataDir = dataDir;
    }

    /**
     *
     * @return
     */
    @Override
    public int getIconId() {
        return CoinManagerFactory.BITCOIN;
    }

    /**
     *
     * @return
     */
    @Override
    public int getCoinId() {
        return CoinManagerFactory.BITCOIN;
    }

    /**
     *
     * @return
     */
    @Override
    public String getCoinSymbol() {
        return null;
    }

    /**
     *
     * @return
     */
    @Override
    public File getDataDir() {
        return _dataDir;
    }


    /**
     *
     * @param wallet
     */
    @Override
    public void setWallet(Wallet wallet) {
        this._wallet = wallet;
    }

    /**
     *
     * @return
     */
    @Override
    public Wallet getWallet() {
        return _wallet;
    }

}
