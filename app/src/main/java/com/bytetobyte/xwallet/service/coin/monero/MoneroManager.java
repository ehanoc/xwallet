package com.bytetobyte.xwallet.service.coin.monero;

import android.content.Context;

import com.bytetobyte.xwallet.service.coin.CoinAction;
import com.bytetobyte.xwallet.service.coin.CoinManager;
import com.bytetobyte.xwallet.service.coin.CurrencyCoin;
import com.bytetobyte.xwallet.service.ipcmodel.CoinTransaction;
import com.bytetobyte.xwallet.service.ipcmodel.SpentValueMessage;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletListener;
import com.m2049r.xmrwallet.model.WalletManager;
import com.m2049r.xmrwallet.service.WalletService;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bruno on 18.10.17.
 */

public class MoneroManager implements CoinManager, CoinAction.CoinActionCallback {

    private static final String TAG = "MoneroManager";

    private final CurrencyCoin<MoneroWalletManager> _coin;
    private final Context _context;

    private WalletManager moneroManagerXmrLib;
    private boolean _isSynced;
    private boolean _isSyncing;
    private boolean _mIsBound;

    private WalletService mBoundService;

    public static final String REQUEST_ID = "id";
    public static final String REQUEST_PW = "pw";
    private static Wallet _wallet;


    private long lastDaemonStatusUpdate = 0;
    private long daemonHeight = 0;
    private Wallet.ConnectionStatus connectionStatus = Wallet.ConnectionStatus.ConnectionStatus_Disconnected;
    private static final long STATUS_UPDATE_INTERVAL = 120000; // 120s (blocktime)

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
        System.out.println("#1");
        moneroManagerXmrLib = WalletManager.getInstance();
        moneroManagerXmrLib.setDaemon("node.moneroworld.com:18089", false, "", "");
        System.out.println("#2");

        _isSyncing = true;
        _isSynced = false;

        File newWalletFile = new File(_coin.getDataDir().getAbsoluteFile() + "/monerowallet");

        boolean hasWallet = moneroManagerXmrLib.walletExists(newWalletFile.getAbsoluteFile());
        System.out.println("has wallet : " + hasWallet);

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

        _wallet.setListener(new WalletListener() {
            public int lastTxCount;
            public long lastBlockTime;

            @Override
            public void moneySpent(String s, long l) {}

            @Override
            public void moneyReceived(String s, long l) {}

            @Override
            public void unconfirmedMoneyReceived(String s, long l) {}

            @Override
            public void newBlock(long height) {
                System.out.println("new block : " + height + " / " + moneroManagerXmrLib.getBlockchainTargetHeight());

                long target = moneroManagerXmrLib.getBlockchainTargetHeight();
                double pct = ((double) height / target) * 100.f;
                callback.onBlocksDownloaded(_coin, pct, target - height, null);

             //   boolean fullRefresh = false;

               // System.out.println("is Synced : " + _wallet.isSynchronized());
                //updateDaemonState(_wallet, _wallet.isSynchronized() ? height : 0);

//                if (!_wallet.isSynchronized()) {
//                    //updated = true;
//                    // we want to see our transactions as they come in
//                    _wallet.getHistory().refresh();
//                    int txCount = _wallet.getHistory().getCount();
//                    if (txCount > lastTxCount) {
//                        // update the transaction list only if we have more than before
//                        lastTxCount = txCount;
//                        fullRefresh = true;
//                        _wallet.store();
//                        _wallet.close();
//                    }
//                }
            }

            @Override
            public void updated() {
                System.out.println("updated()");
            }

            @Override
            public void refreshed() {
                System.out.println("refreshed()");

                if(!_isSynced && _wallet.isSynchronized()) {
                    System.out.println("storing");
                    _isSynced = true;
                    _isSyncing = false;
                    _wallet.store();
                    callback.onChainSynced(_coin);
                   // _wallet.close();
                }

                //updateDaemonState(_wallet, 0);

                _wallet.getHistory().refresh();

                String seed = _wallet.getSeed();
                System.out.println("seed : " + seed);

                Wallet.Status status = _wallet.getStatus();
                System.out.println("status : " + status);

                //daemon info

                System.out.println("daemon addr : " + moneroManagerXmrLib.getDaemonAddress());

                System.out.println("daemon ver : " + moneroManagerXmrLib.getDaemonVersion());

                System.out.println("blockchain height : " + moneroManagerXmrLib.getBlockchainHeight());
            }
        });

        _wallet.init(0);
        _wallet.refresh();
    }

    private void updateDaemonState(Wallet wallet, long height) {
        long t = System.currentTimeMillis();
        if (height > 0) { // if we get a height, we are connected
            daemonHeight = height;
            connectionStatus = Wallet.ConnectionStatus.ConnectionStatus_Connected;
            lastDaemonStatusUpdate = t;
        } else {
            if (t - lastDaemonStatusUpdate > STATUS_UPDATE_INTERVAL) {
                lastDaemonStatusUpdate = t;
                // these calls really connect to the daemon - wasting time
                daemonHeight = wallet.getDaemonBlockChainHeight();
                if (daemonHeight > 0) {
                    // if we get a valid height, then obviously we are connected
                    connectionStatus = Wallet.ConnectionStatus.ConnectionStatus_Connected;
                } else {
                    connectionStatus = Wallet.ConnectionStatus.ConnectionStatus_Disconnected;
                }
            }
        }

        System.out.println("updated daemon status: " + daemonHeight + "/" + connectionStatus.toString());
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
        return null;
    }

    @Override
    public String getBalanceFriendlyStr() {
        return Long.toString(_wallet.getBalance());
    }

    @Override
    public long getBalanceValue() {
        return 0;
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
}
