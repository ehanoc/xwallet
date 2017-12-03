package com.bytetobyte.xwallet.service.coin.monero;

import android.content.Context;

import com.bytetobyte.xwallet.service.Constants;
import com.bytetobyte.xwallet.service.coin.CoinAction;
import com.bytetobyte.xwallet.service.coin.CoinManager;
import com.bytetobyte.xwallet.service.coin.CurrencyCoin;
import com.bytetobyte.xwallet.service.ipcmodel.CoinTransaction;
import com.bytetobyte.xwallet.service.ipcmodel.SpentValueMessage;
import com.m2049r.xmrwallet.model.PendingTransaction;
import com.m2049r.xmrwallet.model.TransactionInfo;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletListener;
import com.m2049r.xmrwallet.model.WalletManager;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bruno on 18.10.17.
 */

public class MoneroManager implements CoinManager, CoinAction.CoinActionCallback, WalletListener {

    private final CurrencyCoin<MoneroWalletManager> _coin;
    private final Context _context;

    private WalletManager moneroManagerXmrLib;
    private boolean _isSynced;
    private boolean _isSyncing;

    private static Wallet _wallet;

    private CoinAction.CoinActionCallback _callback;
    private String _walletPwd;
    private File _walletFile;

    private long _targetHeight;
    private long _recoveryHeight;

    private static String TEST_NODE = "testnet.xmrchain.net:28081";
    private static String DEFAULT_NODE = "node.moneroworld.com:18089";
    private static String NODE = Constants.IS_TESTNET ? TEST_NODE : DEFAULT_NODE;

    final static PendingTransaction.Priority Priorities[] =
            {PendingTransaction.Priority.Priority_Default,
                    PendingTransaction.Priority.Priority_Low,
                    PendingTransaction.Priority.Priority_Medium,
                    PendingTransaction.Priority.Priority_High}; // must match the layout XML

    //pending
    private PendingTransaction _pendingTx;

    /**
     *
     * @param moneroCoin
     */
    public MoneroManager(CurrencyCoin<MoneroWalletManager> moneroCoin, Context context) {
        this._coin = moneroCoin;
        this._context = context;
        _recoveryHeight = 0;
    }

    /**
     *
     * @param callback
     */
    @Override
    public void setup(final CoinAction.CoinActionCallback callback) {
        _callback = callback;

        _walletFile = new File(_coin.getDataDir().getAbsoluteFile() + "/monerowallet_" + Constants.IS_TESTNET + Monero.WALLET_EXTRA_ID);

        System.out.println("MoneroManager::setup");
        moneroManagerXmrLib = WalletManager.getInstance();
        moneroManagerXmrLib.setDaemon(NODE, Constants.IS_TESTNET, "", "");
        _targetHeight = moneroManagerXmrLib.getBlockchainTargetHeight();

        System.out.println("#2");

        _isSyncing = true;
        _isSynced = false;

        boolean hasWallet = moneroManagerXmrLib.walletExists(_walletFile.getAbsoluteFile());
        System.out.println("has wallet : " + hasWallet + " with pwd : " + _walletPwd);

        if (_wallet == null) {
            if (hasWallet) {
                _wallet = moneroManagerXmrLib.openWallet(_walletFile.getAbsolutePath(), _walletPwd);
            } else {
                _wallet = moneroManagerXmrLib.createWallet(_walletFile, _walletPwd, "English");
                boolean rc = _wallet.store();
                System.out.println("wallet stored: " + _wallet.getName() + " with rc=" + rc);
                if (!rc) {
                    System.out.println("Wallet store failed: " + _wallet.getErrorString());
                }
            }
        }

        System.out.println("#3");

        boolean hasInit = _wallet.init(0);
        System.out.println("Wallet has init : " + hasInit);

        _wallet.setListener(this);

        System.out.println("Wallet status : " + _wallet.getStatus());

        boolean isSynced = _wallet.isSynchronized();
        System.out.println("wallet is synced : " + isSynced);

        System.out.println("wallet height : " + _wallet.getBlockChainHeight()
                + " daemon height :" + _wallet.getDaemonBlockChainHeight());

        _wallet.refresh();
    }

    /**
     *
     * @param address
     * @param amountsStr
     * @param callback
     */
    @Override
    public void sendCoins(String address, String amountsStr, Map<Integer, Object> options, CoinAction.CoinActionCallback callback) {
//        PendingTransaction pendingTx = _wallet.createTransaction(address,
//                "",
//                Wallet.getAmountFromString(amountsStr),
//                4,
//                PendingTransaction.Priority.Priority_Default);
//
//        String priorityValue = (String) options.get(Monero.KEY_TX_PRIORITY);
//        Double mixinDouble = (Double) (options.get(Monero.KEY_TX_MIXINS)); //dont know why
//        String paymentId = (String) options.get(Monero.KEY_TX_PAYMENT_ID);
//
//        int mixin = mixinDouble.intValue();

        if (_pendingTx == null) return; // no prepared TX with calculated fee ?

        long amount = Wallet.getAmountFromString(amountsStr);

        if (_pendingTx.getAmount() == amount && _pendingTx.getStatus() == PendingTransaction.Status.Status_Ok) {
            String txid = _pendingTx.getFirstTxId();
            boolean success = _pendingTx.commit("", true);

            System.out.println("Committing :" + txid + " success : " + success);
            _wallet.disposePendingTransaction();
            _pendingTx = null;
        }
//
//        PendingTransaction.Priority priority = PendingTransaction.Priority.valueOf(priorityValue);
//
//        if (amount == _pendingTx.getAmount() && address == _pendingTx.getFirstTxId())
//
//        String txid = pendingTx.getFirstTxId();
//        boolean success = pendingTx.commit("", true);
//
//        System.out.println("Committing :" + txid + " success : " + success);
//        _wallet.disposePendingTransaction();
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

        if (_wallet != null) {
            List<TransactionInfo> moneroTxs = _wallet.getHistory().getAll();
            System.out.println(" nr of txs : " + _wallet.getHistory().getCount());

            for (TransactionInfo tx : moneroTxs) {

                long amount = tx.amount;
                if (tx.direction == TransactionInfo.Direction.Direction_Out)
                    amount *= -1;

                CoinTransaction aNewCoinTx = new CoinTransaction(_coin.getCoinId(),
                        Long.toString(tx.fee),
                        tx.hash,
                        formatAmount(amount),
                        Long.toString(tx.confirmations),
                        new Date(tx.timestamp * 1000)
                );

                allTxs.add(aNewCoinTx);
            }
        }

        return allTxs;
    }

    @Override
    public String getBalanceFriendlyStr() {
        return formatAmount(_wallet.getBalance());
    }

    /**
     *
     * @param amount
     * @return
     */
    private static String formatAmount(long amount) {
        BigDecimal bigDecimal = new BigDecimal(amount);
        String res = bigDecimal
                .scaleByPowerOfTen(-12)
                .stripTrailingZeros()
                .toPlainString();

        return res;
    }

    /**
     *
     * @return
     */
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
        if (_pendingTx != null) {
            _wallet.disposePendingTransaction();
            _pendingTx = null;
        }

        Map<Integer, Object> options = valueMessage.getExtraOptions();

        String priorityValue = (String) options.get(Monero.KEY_TX_PRIORITY);
        Double mixinDouble = (Double) (options.get(Monero.KEY_TX_MIXINS)); //dont know why
        String paymentId = (String) options.get(Monero.KEY_TX_PAYMENT_ID);

        int mixin = mixinDouble.intValue();
        long amount = Wallet.getAmountFromString(valueMessage.getAmount());
        String address = valueMessage.getAddress();
        PendingTransaction.Priority priority = PendingTransaction.Priority.valueOf(priorityValue);

        _pendingTx = _wallet.createTransaction(address,
                paymentId,
                amount,
                mixin,
                priority);

        long fee = _pendingTx.getFee();
        String feeAmount = Wallet.getDisplayAmount(fee);
        System.out.println("Fee amount calculated " + feeAmount + " (" + fee + ")");

        valueMessage.setTxFee(feeAmount);

        System.out.println(" tx status : " + _pendingTx.getStatus());
        System.out.println(" tx error : " + _pendingTx.getErrorString());

        // we just want to calculate the tx for now, dispose
        //_wallet.disposeTransaction(pendingTx);

        return valueMessage;
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

    /**
     *
     * @return
     */
    @Override
    public Map<String, String> getAddressesKeys() {
        Map<String, String> keys = new LinkedHashMap<>();

        keys.put("spend", _wallet.getSecretSpendKey());
        keys.put("view", _wallet.getSecretViewKey());
        keys.put("public address", _wallet.getAddress());

        return keys;
    }

    /**
     *
     * @return
     */
    @Override
    public String getMnemonicSeed() {
        return _wallet.getSeed();
    }

    /**
     *
     * @return
     */
    @Override
    public Date getMnemonicSeedCreationDate() {
        return null;
    }

    /**
     *
     * @param callback
     * @param input
     * @param creationdDate
     */
    @Override
    public void recoverWalletBy(CoinAction.CoinActionCallback callback, String input, Date creationdDate, long blockheight, boolean isViewOnly) {
        _callback = callback;

        _recoveryHeight = blockheight;
        _isSyncing = true;
        _isSynced = false;
//
//        if (_wallet != null) {
//            _wallet.pauseRefresh();
//            _wallet.close();
//        }

        File newWalletFile = new File(_coin.getDataDir().getAbsoluteFile() + "/monerowallet_" + Constants.IS_TESTNET + Monero.WALLET_EXTRA_ID);

        for(File file: _coin.getDataDir().listFiles()) {
            if (!file.isDirectory()) {
                boolean isDeleted = file.delete();
                System.out.println("File : " + file.getName() + " was deleted : " + isDeleted);
            }
        }

        if (moneroManagerXmrLib == null) {
            moneroManagerXmrLib = WalletManager.getInstance();
            moneroManagerXmrLib.setDaemon(NODE, Constants.IS_TESTNET, "", "");
        }

        _targetHeight = moneroManagerXmrLib.getBlockchainTargetHeight();
        if (!isViewOnly) {
            _wallet = moneroManagerXmrLib.recoveryWallet(newWalletFile, input, _recoveryHeight);
        } else {
            String[] inputTokens = input.trim().split(":");
            _wallet = moneroManagerXmrLib.createWalletFromKeys(newWalletFile, "English", _recoveryHeight, inputTokens[0], inputTokens[1], "");
        }
        _wallet.setPassword(_walletPwd);
//        _wallet.setSeedLanguage("English");
//        _wallet.store();
//

        System.out.println("Recovery wallet status : " + _wallet.getStatus().name());
        if (_wallet.getStatus() != Wallet.Status.Status_Ok) {
            System.out.println("Wallet error : " + _wallet.getErrorString());
        }

//        boolean rc = _wallet.store();
//        System.out.println("wallet stored: " + _wallet.getName() + " with rc=" + rc);
//        if (!rc) {
//            System.out.println("Wallet store failed: " + _wallet.getErrorString());
//        }

        boolean hasInit = _wallet.init(0);
        System.out.println("Wallet has init : " + hasInit);
        _wallet.setListener(this);

        _wallet.refresh();
    }

    /**
     *
     */
    @Override
    public void stopSync() {
        if (_wallet != null) {
            _wallet.pauseRefresh();
            _wallet.store();
            _wallet.close();
        }
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
        if (_wallet != null) {
            System.out.println("Storing wallet");
            boolean isStored = _wallet.store();
            System.out.println("wallet stored : " + isStored);
            _wallet.close();
        }
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
        System.out.println("moneySpent addr : " + s + " amount : " + l);

        //sendCoins(s, Long.toString(l), _callback);
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
        System.out.println("new block : " + height);

        double pct = ((double) height / _targetHeight) * 100.0f;

        try {
            DecimalFormat df = new DecimalFormat("#.000");
            pct = Double.valueOf(df.format(pct));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (height % 5000 == 0)
            _wallet.getHistory().refresh();

        if (_recoveryHeight > 0 && height > _recoveryHeight) { // we are recovery, long process, store often
            if (height % 15000 == 0) {
                System.out.println("storing...");
                _wallet.store();
            }
        }

//        pct = Math.round(pct * 100.000f);
//        pct = pct / 100.000f;

        if (_callback != null) {
            _callback.onBlocksDownloaded(_coin, pct, _targetHeight - height, null);
        }
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

        _wallet.getHistory().refresh();

        if(_isSynced) {
            if (_callback != null)
                _callback.onChainSynced(_coin);
            // _wallet.close();
        }

       // updateDaemonState(_wallet, 0);
        System.out.println("Wallet status : " + _wallet.getStatus());
        if (_wallet.getStatus() != Wallet.Status.Status_Ok) {
            System.out.println(" Wallet error : " + _wallet.getErrorString());
        }

        //daemon info
        System.out.println("daemon addr : " + moneroManagerXmrLib.getDaemonAddress());
        System.out.println("daemon ver : " + moneroManagerXmrLib.getDaemonVersion());
        System.out.println("blockchain height : " + moneroManagerXmrLib.getBlockchainHeight());
    }

    /**
     *
     * @param wallet
     * @param height
     */
//    private void updateDaemonState(Wallet wallet, long height) {
//        long t = System.currentTimeMillis();
//        if (height > 0) { // if we get a height, we are connected
//            daemonHeight = height;
//            connectionStatus = Wallet.ConnectionStatus.ConnectionStatus_Connected;
//            lastDaemonStatusUpdate = t;
//        } else {
//            if (t - lastDaemonStatusUpdate > STATUS_UPDATE_INTERVAL) {
//                lastDaemonStatusUpdate = t;
//                // these calls really connect to the daemon - wasting time
//                daemonHeight = wallet.getDaemonBlockChainHeight();
//                if (daemonHeight > 0) {
//                    // if we get a valid height, then obviously we are connected
//                    connectionStatus = Wallet.ConnectionStatus.ConnectionStatus_Connected;
//                } else {
//                    connectionStatus = Wallet.ConnectionStatus.ConnectionStatus_Disconnected;
//                }
//            }
//        }
//        //Log.d(TAG, "updated daemon status: " + daemonHeight + "/" + connectionStatus.toString());
//    }

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
