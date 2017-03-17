package com.bytetobyte.xwallet.service.coin;

import android.content.Context;

import com.bytetobyte.xwallet.service.coin.bitcoin.Bitcoin;
import com.bytetobyte.xwallet.service.coin.bitcoin.BitcoinManager;

import java.io.File;

/**
 * Created by bruno on 22.03.17.
 */
public abstract class CoinManagerFactory {

    /**
     *
     */
    public final static int BITCOIN = 0x1;

    /**
     *
     * @param coinInt
     * @return
     */
    public static CoinManager getCoinManagerBy(Context context, int coinInt) {

        CoinManager selected = null;
        File dataDir = null;

        /**
         *
         */
        switch (coinInt) {
            case BITCOIN:
                dataDir = context.getDir(Bitcoin.BITCOIN_DATA_DIR_NAME, Context.MODE_PRIVATE);
                selected = new BitcoinManager(new Bitcoin(dataDir));
                break;

            default:
                dataDir = context.getDir(Bitcoin.BITCOIN_DATA_DIR_NAME, Context.MODE_PRIVATE);
                selected = new BitcoinManager(new Bitcoin(dataDir));
                break;
        }

        return selected;
    }
}
