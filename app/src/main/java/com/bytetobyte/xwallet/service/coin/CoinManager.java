package com.bytetobyte.xwallet.service.coin;

import java.util.List;
import java.util.Map;

/**
 * Created by bruno on 21.03.17.
 */
public interface CoinManager {
    void setup(CoinAction.CoinActionCallback callback);
    void sendCoins(String address, long amount, CoinAction.CoinActionCallback callback);
    void onCoinsReceived();

    void onReady();

    String getBalanceFriendlyStr();
    long getBalanceValue();

    public CurrencyCoin getCurrencyCoin();
    public List<String> getCurrentAddresses();
    public Map<String, String> getAddressesKeys();

    public String getMnemonicSeed();
    void recoverWalletBy(CoinAction.CoinActionCallback callback, String seed);

    boolean isSyncing();
    boolean isSynced();
}
