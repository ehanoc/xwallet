package com.bytetobyte.xwallet;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.bytetobyte.xwallet.service.BlockchainService;
import com.bytetobyte.xwallet.service.ipc.BlockDownloaded;
import com.bytetobyte.xwallet.service.ipc.SyncedMessage;
import com.google.gson.Gson;

/**
 * Created by bruno on 22.03.17.
 */
public abstract class XWalletBaseActivity extends AppCompatActivity {

    private Gson _gson;

    protected abstract void onServiceReady();
    protected abstract void onSyncReady(SyncedMessage syncedMessage);
    protected abstract void onBlockDownloaded(BlockDownloaded block);

    /**
     *
     * @param msg
     */
    protected void sendMessage(Message msg) {
        if (!mBound) return;
//        // Create and send a message to the service, using a supported 'what' value
//        Message msg = Message.obtain(null, BlockchainService.MSG_SAY_HELLO, 0, 0);
        try {
            msg.replyTo = new Messenger(new ResponseHandler());

            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    @Override
    protected void onStart() {
        super.onStart();
        // Bind to the service
        bindService(new Intent(this, BlockchainService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     *
     */
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (_gson == null) {
            _gson = new Gson();
        }
    }

    /**
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    // This class handles the Service response
    class ResponseHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            int respCode = msg.what;

            //System.out.println("XWalletBaseActivity#ResponseHandler::handleMessage msg : " + respCode);

            switch (respCode) {

                case BlockchainService.IPC_MSG_WALLET_SYNC:
                    String spentJson = msg.getData().getString(BlockchainService.IPC_BUNDLE_DATA_KEY);
                    SyncedMessage syncedMessage = _gson.fromJson(spentJson, SyncedMessage.class);
                    onSyncReady(syncedMessage);
                    break;

                case BlockchainService.IPC_MSG_WALLET_BLOCK_DOWNLOADED:
                    String blockJson = msg.getData().getString(BlockchainService.IPC_BUNDLE_DATA_KEY);
                    BlockDownloaded blocksDownloadMsg = _gson.fromJson(blockJson, BlockDownloaded.class);
                    onBlockDownloaded(blocksDownloadMsg);
                    break;

                default:
                    break;
            }
        }

    }


    /** Messenger for communicating with the service. */
    Messenger mService = null;

    /** Flag indicating whether we have called bind on the service. */
    boolean mBound;

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the object we can use to
            // interact with the service.  We are communicating with the
            // service using a Messenger, so here we get a client-side
            // representation of that from the raw IBinder object.
            mService = new Messenger(service);
            mBound = true;

            onServiceReady();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mBound = false;
        }
    };

}
