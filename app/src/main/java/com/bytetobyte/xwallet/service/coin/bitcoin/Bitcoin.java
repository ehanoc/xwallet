package com.bytetobyte.xwallet.service.coin.bitcoin;

import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.coin.CurrencyCoin;

import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.Wallet;

import java.io.File;

/**
 * Created by bruno on 22.03.17.
 */
public class Bitcoin implements CurrencyCoin<WalletAppKit> {

    /**
     *
     */
    private File _dataDir = null;
    public static final String BITCOIN_DATA_DIR_NAME = "bitcoinDataDir";
    private WalletAppKit _walletKit;
    private Class<WalletAppKit> _walletKitClassType;

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
    public void setWallet(WalletAppKit wallet) {
        this._walletKit = wallet;
    }

    /**
     *
     * @return
     */
    @Override
    public WalletAppKit getWalletManager() {
        return _walletKit;
    }
}
