package com.bytetobyte.xwallet.service.coin;

import android.content.Context;

import com.bytetobyte.xwallet.service.coin.bitcoin.Bitcoin;
import com.bytetobyte.xwallet.service.coin.bitcoin.BitcoinManager;
import com.bytetobyte.xwallet.service.coin.monero.Monero;
import com.bytetobyte.xwallet.service.coin.monero.MoneroManager;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by bruno on 22.03.17.
 */
public abstract class CoinManagerFactory {

    /**
     *
     */
    public final static int BITCOIN = 0x1;
    public final static int MONERO = 0x2;

    private static Map<Integer, CoinManager> coinManagerMap = new LinkedHashMap<>();
    /**
     *
     * @param coinInt
     * @return
     */
    public static CoinManager getCoinManagerBy(Context context, int coinInt) {

        CoinManager selected = null;
        File dataDir = null;

        if (coinManagerMap.containsKey(coinInt) && coinManagerMap.get(coinInt) != null) {
            return coinManagerMap.get(coinInt);
        }

        /**
         *
         */
        switch (coinInt) {
            case BITCOIN:
                dataDir = context.getDir(Bitcoin.BITCOIN_DATA_DIR_NAME, Context.MODE_PRIVATE);
                selected = new BitcoinManager(new Bitcoin(dataDir));
                break;

            case MONERO:
                dataDir = context.getDir(Monero.MONERO_DATA_DIR_NAME, Context.MODE_PRIVATE);
                selected = new MoneroManager(new Monero(dataDir), context);
                break;

            default:
                dataDir = context.getDir(Bitcoin.BITCOIN_DATA_DIR_NAME, Context.MODE_PRIVATE);
                selected = new BitcoinManager(new Bitcoin(dataDir));
                break;
        }

        coinManagerMap.put(coinInt, selected);

        return selected;
    }
}
