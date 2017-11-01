package com.bytetobyte.xwallet.service.coin.monero;

import android.content.Context;

import com.bytetobyte.xwallet.service.coin.CoinAction;
import com.bytetobyte.xwallet.service.coin.CoinManager;
import com.bytetobyte.xwallet.service.coin.CurrencyCoin;
import com.bytetobyte.xwallet.service.ipcmodel.CoinTransaction;
import com.bytetobyte.xwallet.service.ipcmodel.SpentValueMessage;
import com.m2049r.xmrwallet.model.TransactionInfo;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletListener;
import com.m2049r.xmrwallet.model.WalletManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bruno on 18.10.17.
 */

public class MoneroManager implements CoinManager, CoinAction.CoinActionCallback, WalletListener {

    private static final String TAG = "MoneroManager";

    private final CurrencyCoin<MoneroWalletManager> _coin;
    private final Context _context;

    private WalletManager moneroManagerXmrLib;
    private boolean _isSynced;
    private boolean _isSyncing;

    private static Wallet _wallet;

    private CoinAction.CoinActionCallback _callback;
    private String _walletPwd;

    /**
     *
     * @param moneroCoin
     */
    public MoneroManager(CurrencyCoin<MoneroWalletManager> moneroCoin, Context context) {
        this._coin = moneroCoin;
        this._context = context;
    }

    /**
     *
     * @param callback
     */
    @Override
    public void setup(final CoinAction.CoinActionCallback callback) {
        _callback = callback;

        System.out.println("MoneroManager::setup");
        moneroManagerXmrLib = WalletManager.getInstance();
        moneroManagerXmrLib.setDaemon("node.moneroworld.com:18089", Monero.IS_TEST_NETWORK, "", "");
        System.out.println("#2");

        _isSyncing = true;
        _isSynced = false;

        File newWalletFile = new File(_coin.getDataDir().getAbsoluteFile() + "/monerowallet_" + Monero.IS_TEST_NETWORK);

        boolean hasWallet = moneroManagerXmrLib.walletExists(newWalletFile.getAbsoluteFile());
        System.out.println("has wallet : " + hasWallet + " with pwd : " + _walletPwd);

        if (_wallet == null) {
            if (hasWallet) {
                _wallet = moneroManagerXmrLib.openWallet(newWalletFile.getAbsolutePath(), "");
            } else {
                _wallet = moneroManagerXmrLib.createWallet(newWalletFile, "", "English");
                boolean rc = _wallet.store();
                System.out.println("wallet stored: " + _wallet.getName() + " with rc=" + rc);
                if (!rc) {
                    System.out.println("Wallet store failed: " + _wallet.getErrorString());
                }
            }
        }

        boolean isSynced = _wallet.isSynchronized();
        System.out.println("wallet is synced : " + isSynced);

        System.out.println("wallet height : " + _wallet.getBlockChainHeight()
                + " daemon height" + _wallet.getDaemonBlockChainHeight());

        System.out.println("#3");

        _wallet.setListener(this);

        _wallet.init(0);
        _wallet.refresh();
    }

    /**
     *
     * @param address
     * @param amount
     * @param callback
     */
    @Override
    public void sendCoins(String address, String amount, CoinAction.CoinActionCallback callback) {

    }

    /**
     *
     */
    @Override
    public void onCoinsReceived() {

    }

    @Override
    public void onReady() {

    }

    @Override
    public List<CoinTransaction> getTransactionList() {
        // Map monero model Txs to generic CoinTransaction
        List<CoinTransaction> allTxs = new ArrayList<>();

        List<TransactionInfo> moneroTxs = _wallet.getHistory().getAll();
        for (TransactionInfo tx: moneroTxs) {
            CoinTransaction aNewCoinTx = new CoinTransaction(_coin.getCoinId(),
                    Long.toString(tx.fee),
                    tx.hash,
                    Long.toString(tx.amount),
                    Long.toString(tx.confirmations),
                    new Date(tx.timestamp)
                    );

            allTxs.add(aNewCoinTx);
        }

        return allTxs;
    }

    @Override
    public String getBalanceFriendlyStr() {
        return Long.toString(_wallet.getBalance());
    }

    @Override
    public long getBalanceValue() {
        return _wallet.getBalance();
    }

    /**
     *
     * @param valueMessage
     * @return
     */
    @Override
    public SpentValueMessage applyTxFee(SpentValueMessage valueMessage) {
        return null;
    }

    /**
     *
     * @return
     */
    @Override
    public CurrencyCoin getCurrencyCoin() {
        return _coin;
    }

    /**
     *
     * @return
     */
    @Override
    public List<String> getCurrentAddresses() {
        List<String> addrs = new ArrayList<>();
        addrs.add(_wallet.getAddress());
        return addrs;
    }

    @Override
    public Map<String, String> getAddressesKeys() {
        Map<String, String> keys = new LinkedHashMap<>();

        keys.put("spend", _wallet.getSecretSpendKey());
        keys.put("view", _wallet.getSecretViewKey());

        return keys;
    }

    @Override
    public String getMnemonicSeed() {
        return _wallet.getSeed();
    }

    @Override
    public Date getMnemonicSeedCreationDate() {
        return null;
    }

    @Override
    public void recoverWalletBy(CoinAction.CoinActionCallback callback, String seed, Date creationdDate) {

    }

    @Override
    public void stopSync() {

    }

    /**
     *
     * @return
     */
    @Override
    public boolean isSyncing() {
        return _isSyncing;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isSynced() {
        return _isSynced;
    }

    /**
     *
     * @param result
     */
    @Override
    public void onResult(Object result) {
        System.out.println("isSyncing :: onResult");

        _isSyncing = false;
    }

    /**
     *
     * @param result
     */
    @Override
    public void onError(Object result) {
        System.out.println("isSyncing :: onError");

        _isSyncing = false;
    }

    /**
     *
     * @param coin
     */
    @Override
    public void onChainSynced(Object coin) {
        System.out.println("onChainSynced");

        _isSyncing = false;
        _isSynced = true;
    }

    /**
     *
     * @param addressStr
     * @param value
     * @param coin
     */
    @Override
    public void onCoinsReceived(String addressStr, long value, Object coin) {
        System.out.println("isSyncing :: onCoinsReceived");
        _isSyncing = false;

        if (_callback == null) return;
        _callback.onCoinsReceived(addressStr, value, coin);
    }

    /**
     *  @param coin
     * @param pct
     * @param blocksLeft
     * @param date
     */
    @Override
    public void onBlocksDownloaded(CurrencyCoin coin, double pct, long blocksLeft, Date date) {

    }

    /**
     *
     */
    @Override
    public void onCloseWallet() {

    }

    // =========================
    // MONERO WALLET LISTENER
    // =========================
    /**
     *
     * @param s
     * @param l
     */
    @Override
    public void moneySpent(String s, long l) {
        sendCoins(s, Long.toString(l), _callback);
    }

    /**
     *
     * @param s
     * @param l
     */
    @Override
    public void moneyReceived(String s, long l) {
        onCoinsReceived(s, l, _coin);
    }

    /**
     *
     * @param s
     * @param l
     */
    @Override
    public void unconfirmedMoneyReceived(String s, long l) {

    }

    /**
     *
     * @param height - block height
     */
    @Override
    public void newBlock(long height) {
        System.out.println("new block : " + height + " / " + moneroManagerXmrLib.getBlockchainTargetHeight());

        long target = moneroManagerXmrLib.getBlockchainTargetHeight();
        double pct = ((double) height / target) * 100.00f;

//        pct = Math.round(pct * 100.0f);
//        pct = pct / 100;

        if (_callback != null)
            _callback.onBlocksDownloaded(_coin, pct, target - height, null);
    }

    /**
     *
     */
    @Override
    public void updated() {
        System.out.println("updated()");
    }

    /**
     *
     */
    @Override
    public void refreshed() {
        System.out.println("refreshed() is synced : " + _wallet.isSynchronized());
        _isSynced = _wallet.isSynchronized();
        _isSyncing = false;

        if(_isSynced) {
            _wallet.store();
            System.out.println("storing");
            if (_callback != null)
                _callback.onChainSynced(_coin);
            // _wallet.close();

            _wallet.getHistory().refresh();
        }

        //updateDaemonState(_wallet, 0);

        //daemon info
        System.out.println("daemon addr : " + moneroManagerXmrLib.getDaemonAddress());
        System.out.println("daemon ver : " + moneroManagerXmrLib.getDaemonVersion());
        System.out.println("blockchain height : " + moneroManagerXmrLib.getBlockchainHeight());
    }

    /**
     *
     * @param walletPwd
     */
    @Override
    public void setWalletPwd(String walletPwd) {
        this._walletPwd = walletPwd;
    }

    /**
     *
     * @return
     */
    @Override
    public String getWalletPwd() {
        return _walletPwd;
    }
}
