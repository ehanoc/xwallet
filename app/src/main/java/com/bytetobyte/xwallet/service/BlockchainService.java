package com.bytetobyte.xwallet.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;

import com.bytetobyte.xwallet.service.coin.CoinAction;
import com.bytetobyte.xwallet.service.coin.CoinManager;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.coin.CurrencyCoin;
import com.bytetobyte.xwallet.service.ipc.BlockDownloaded;
import com.bytetobyte.xwallet.service.ipc.SpentValueMessage;
import com.bytetobyte.xwallet.service.ipc.SyncedMessage;
import com.google.gson.Gson;

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

    // ###############################
    // IPC BUNDLE DATA
    // ###############################
    public static final String IPC_BUNDLE_DATA_KEY = "IPC_BUNDLE_DATA_KEY";

    // ###############################
    // Local Broadcasts
    // ###############################
    public static final int BROADCAST_MSG_COINS_RECEIVED = 0x3;

    private final Gson _gson;

    private static CoinManager _coinManager;

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());
    private static Messenger _replyTo;

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
        System.out.println("BlockchainService CurrencyCoin SYNCED!!");

        List<String> addrs = _coinManager.getCurrentAddresses();
        for (String addr : addrs) {
            System.out.println("Address : " + addr);
        }

        System.out.println("Wallet seed : " + _coinManager.getMnemonicSeed());

        Set<Map.Entry<String, String>> addrKeyEntrySet = _coinManager.getAddressesKeys().entrySet();
        for (Map.Entry<String, String> entry : addrKeyEntrySet) {
            System.out.println("Addr : " + entry.getKey() + ", Key :" + entry.getValue());
        }

        System.out.println("Bitcoin balance : " + _coinManager.getBalanceFriendlyStr());
        System.out.println("Bitcoin balance value : " + _coinManager.getBalanceValue());

        SyncedMessage syncedMessage = new SyncedMessage(_coinManager.getBalanceFriendlyStr(), addrs);
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

            System.out.println("isSyncing coinManager : " + _coinManager);

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

                    PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BlockchainServiceLockTag");

                    long lockTime = 1000 * 360 * 5;
                    wakeLock.acquire(lockTime);

                    _coinManager.setup(BlockchainService.this);
                    break;

                case IPC_MSG_WALLET_RECOVER:
                    _coinManager.recoverWalletBy(BlockchainService.this, "illness bulk jewel deer chaos swing goose fetch patch blood acid call creation");
                    break;

                case IPC_MSG_WALLET_SEND_AMOUNT:
                    String spentJson = msg.getData().getString(IPC_BUNDLE_DATA_KEY);
                    SpentValueMessage spentValueMsg = _gson.fromJson(spentJson, SpentValueMessage.class);

                    _coinManager.sendCoins(spentValueMsg.getAddress(), spentValueMsg.getAmount(), BlockchainService.this);
                    break;

                default:
                    super.handleMessage(msg);
            }
        }
    }
}
