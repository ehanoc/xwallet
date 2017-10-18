package com.bytetobyte.xwallet.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;

import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.service.coin.CoinAction;
import com.bytetobyte.xwallet.service.coin.CoinManager;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.coin.CurrencyCoin;
import com.bytetobyte.xwallet.service.coin.monero.MoneroWallet;
import com.bytetobyte.xwallet.service.ipcmodel.BlockDownloaded;
import com.bytetobyte.xwallet.service.ipcmodel.CoinTransaction;
import com.bytetobyte.xwallet.service.ipcmodel.MnemonicSeedBackup;
import com.bytetobyte.xwallet.service.ipcmodel.RecoverWalletMessage;
import com.bytetobyte.xwallet.service.ipcmodel.SpentValueMessage;
import com.bytetobyte.xwallet.service.ipcmodel.SyncedMessage;
import com.google.gson.Gson;
import com.m2049r.xmrwallet.model.Wallet;
import com.m2049r.xmrwallet.model.WalletManager;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by bruno on 21.03.17.
 */
public class BlockchainService extends Service implements CoinAction.CoinActionCallback<CurrencyCoin> {

    // ###############################
    // Attributes
    // ###############################
    public static final int IPC_MSG_WALLET_SYNC = 0x0;
    public static final int IPC_MSG_WALLET_RECOVER = 0x1;
    public static final int IPC_MSG_WALLET_SEND_AMOUNT = 0x2;
    public static final int IPC_MSG_WALLET_BLOCK_DOWNLOADED = 0x3;
    public static final int IPC_MSG_WALLET_CALCULATE_FEE = 0x4;
    public static final int IPC_MSG_WALLET_TRANSACTION_LIST = 0x5;
    public static final int IPC_MSG_WALLET_MNENOMIC_SEED = 0x6;

    // ###############################
    // IPC BUNDLE DATA
    // ###############################
    public static final String IPC_BUNDLE_DATA_KEY = "IPC_BUNDLE_DATA_KEY";

    // ###############################
    // Local Broadcasts
    // ###############################
    public static final int BROADCAST_MSG_COINS_RECEIVED = 0x3;


    private static final int NOTIFICATION_SYNC_ID = 0x78;

    private final Gson _gson;

    private static CoinManager _coinManager;

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private static Messenger _replyTo;

    private static PowerManager.WakeLock _wakeLock;
    private static WifiManager.WifiLock _wifiLock;

    /**
     *
     */
    public BlockchainService() {
        _gson = new Gson();
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    /**
     *
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     *
     * @param result
     */
    @Override
    public void onResult(CurrencyCoin result) {

    }

    /**
     *
     * @param result
     */
    @Override
    public void onError(CurrencyCoin result) {

    }

    /**
     *
     * @param coin
     */
    @Override
    public void onChainSynced(CurrencyCoin coin) {
        stopForeground(true);
        releaseWakeLocks();

       // WalletManager manager = WalletManager.getInstance();
        //manager.setDaemon();
      //  Wallet w = manager.createWallet(getBaseContext().getFilesDir(), "", "");

        System.out.println("BlockchainService CurrencyCoin SYNCED!! ");

        List<String> addrs = _coinManager.getCurrentAddresses();
//        for (String addr : addrs) {
//            System.out.println("Address : " + addr);
//        }

        //System.out.println("Wallet seed : " + _coinManager.getMnemonicSeed());

 //       Set<Map.Entry<String, String>> addrKeyEntrySet = _coinManager.getAddressesKeys().entrySet();
//        for (Map.Entry<String, String> entry : addrKeyEntrySet) {
//            System.out.println("Addr : " + entry.getKey() + ", Key :" + entry.getValue());
//        }

 //       System.out.println("Bitcoin balance : " + _coinManager.getBalanceFriendlyStr());
 //       System.out.println("Bitcoin balance value : " + _coinManager.getBalanceValue());

        SyncedMessage syncedMessage = new SyncedMessage(_coinManager.getCurrencyCoin().getCoinId(), _coinManager.getBalanceFriendlyStr(), addrs);
        Message toReply = Message.obtain(null, IPC_MSG_WALLET_SYNC);
        toReply.getData().putString(IPC_BUNDLE_DATA_KEY, _gson.toJson(syncedMessage));

        replyMessage(toReply);
    }

    /**
     *
     * @param addressStr
     * @param value
     * @param coin
     */
    @Override
    public void onCoinsReceived(String addressStr, long value, CurrencyCoin coin) {
        System.out.println("onCoinsReceived ! add : " + addressStr + " value: " + value);
    }

    /**
     *
     * @param coin
     * @param pct
     * @param blocksSoFar
     * @param date
     */
    @Override
    public void onBlocksDownloaded(CurrencyCoin coin, double pct, int blocksSoFar, Date date) {
        String text = "Syncing : " + pct + " %";

        Notification notification = getServiceNotification(text);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(NOTIFICATION_SYNC_ID, notification);

        BlockDownloaded blockDownloaded = new BlockDownloaded(coin, pct, blocksSoFar, date);
        Message toReply = Message.obtain(null, IPC_MSG_WALLET_BLOCK_DOWNLOADED);
        toReply.getData().putString(IPC_BUNDLE_DATA_KEY, _gson.toJson(blockDownloaded));
        replyMessage(toReply);
    }

    /**
     *
     * @param msg
     */
    private void replyMessage(Message msg) {
        try {
            _replyTo.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            _replyTo = msg.replyTo;

           // System.out.println("isSyncing coinManager : " + _coinManager);

            if (_coinManager == null)
                _coinManager = CoinManagerFactory.getCoinManagerBy(getBaseContext(), msg.arg1);

            System.out.println("BlockchainService handling message! isSyncing : " + _coinManager.isSyncing());

            if (_coinManager.isSyncing())
                return;

            switch (msg.what) {
                case IPC_MSG_WALLET_SYNC:
                    // Just return info to client
                    if (_coinManager.isSynced()) {
                        onChainSynced(_coinManager.getCurrencyCoin());
                        return;
                    }

                   acquireWakeLocks();

                    System.out.println("service setup! : " + _coinManager);
                    startForeground(NOTIFICATION_SYNC_ID, getServiceNotification("Starting"));
                    _coinManager.setup(BlockchainService.this);
                    break;

                case IPC_MSG_WALLET_RECOVER:
                    RecoverWalletMessage recoverMsg = _gson.fromJson(msg.getData().getString(IPC_BUNDLE_DATA_KEY), RecoverWalletMessage.class);

                    acquireWakeLocks();
                    //wakeLock.acquire(1000 * 360 * 5);
                    startForeground(NOTIFICATION_SYNC_ID, getServiceNotification("Starting"));
                    // illness bulk jewel deer chaos swing goose fetch patch blood acid call creation
                    System.out.println("service recover! : " + _coinManager);
                    _coinManager.recoverWalletBy(BlockchainService.this, recoverMsg.getSeed(), recoverMsg.getDate());
                    break;

                case IPC_MSG_WALLET_SEND_AMOUNT:
                    String spentJson = msg.getData().getString(IPC_BUNDLE_DATA_KEY);
                    SpentValueMessage spentValueMsg = _gson.fromJson(spentJson, SpentValueMessage.class);
                    _coinManager.sendCoins(spentValueMsg.getAddress(), spentValueMsg.getAmount(), BlockchainService.this);
                    break;

                case IPC_MSG_WALLET_CALCULATE_FEE:
                    String spentToCalculateFee = msg.getData().getString(IPC_BUNDLE_DATA_KEY);

                    SpentValueMessage spentMsgForFee = _gson.fromJson(spentToCalculateFee, SpentValueMessage.class);
                    spentMsgForFee = _coinManager.applyTxFee(spentMsgForFee);

                    Message toReply = Message.obtain(null, IPC_MSG_WALLET_CALCULATE_FEE);
                    toReply.getData().putString(IPC_BUNDLE_DATA_KEY, _gson.toJson(spentMsgForFee));
                    replyMessage(toReply);
                    break;

                case IPC_MSG_WALLET_TRANSACTION_LIST:
                    List<CoinTransaction> txs = _coinManager.getTransactionList();

                    Message txReply = Message.obtain(null, IPC_MSG_WALLET_TRANSACTION_LIST);
                    txReply.getData().putString(IPC_BUNDLE_DATA_KEY, _gson.toJson(txs));
                    replyMessage(txReply);
                    break;

                case IPC_MSG_WALLET_MNENOMIC_SEED:
                    String seed = _coinManager.getMnemonicSeed();
                    Date seedCreationDate = _coinManager.getMnemonicSeedCreationDate();
                    Map<String, String> addrsAndKeys = _coinManager.getAddressesKeys();

                    MnemonicSeedBackup seedBackup = new MnemonicSeedBackup(seed, seedCreationDate, addrsAndKeys);
                    Message seedReplyMsg = Message.obtain(null, IPC_MSG_WALLET_MNENOMIC_SEED);
                    seedReplyMsg.getData().putString(IPC_BUNDLE_DATA_KEY, _gson.toJson(seedBackup));
                    replyMessage(seedReplyMsg);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     *
     * @return
     */
    private Notification getServiceNotification (String text) {
       // Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.logo);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(getString(R.string.service_notification_title))
                .setContentText(text)
                .setSmallIcon(R.drawable.logo)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setSound(null)
                .setVibrate(null);

        //builder.setSmallIcon(xx);
        builder.setDefaults(Notification.DEFAULT_LIGHTS);

        Notification not = builder.build();
        not.flags = Notification.FLAG_FOREGROUND_SERVICE;

        return not;
    }

    /**
     *
     */
    private void acquireWakeLocks() {
        if(_wakeLock == null) {
            PowerManager powerManager = (PowerManager) getApplicationContext().getSystemService(POWER_SERVICE);
            _wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BlockchainServiceLockTag");
        }

        if (_wifiLock == null) {
            WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            _wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "BlockchainServiceWifiLockTag");
        }

        if (!_wakeLock.isHeld())
            _wakeLock.acquire();

        if (!_wifiLock.isHeld())
            _wifiLock.acquire();
    }

    /**
     *
     */
    private void releaseWakeLocks() {
        if (_wakeLock.isHeld())
            _wakeLock.release();

        if (_wifiLock.isHeld())
            _wifiLock.release();
    }
}
