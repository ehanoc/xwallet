package com.bytetobyte.xwallet.service.coin;

import java.io.File;

/**
 * Created by bruno on 22.03.17.
 */
public interface CurrencyCoin<W> {

    public int getIconId();
    
    public int getCoinId();

    public String getCoinSymbol();

    /**
     *  Where to store data files, such as the blockchain files
     * @return
     */
    public File getDataDir();

    /**
     *
     * @param wallet
     */
    public void setWalletManager(W wallet);
    public W getWalletManager();
}
