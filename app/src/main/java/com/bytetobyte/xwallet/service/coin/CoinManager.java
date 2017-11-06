package com.bytetobyte.xwallet.service.coin;

import com.bytetobyte.xwallet.service.ipcmodel.CoinTransaction;
import com.bytetobyte.xwallet.service.ipcmodel.SpentValueMessage;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by bruno on 21.03.17.
 */
public interface CoinManager {
    void setup(CoinAction.CoinActionCallback callback);
    void sendCoins(String address, String amount, CoinAction.CoinActionCallback callback);
    void onCoinsReceived();

    void onReady();

    List<CoinTransaction> getTransactionList();

    String getBalanceFriendlyStr();
    long getBalanceValue();
    SpentValueMessage applyTxFee(SpentValueMessage valueMessage);

    public CurrencyCoin getCurrencyCoin();
    public List<String> getCurrentAddresses();
    public Map<String, String> getAddressesKeys();

    public String getMnemonicSeed();
    public Date getMnemonicSeedCreationDate();

    void recoverWalletBy(CoinAction.CoinActionCallback callback, String seed, Date creationdDate, long blockHeight);

    void stopSync();

    boolean isSyncing();
    boolean isSynced();

    void setWalletPwd(String walletPwd);
    String getWalletPwd();
    void onCloseWallet();
}
