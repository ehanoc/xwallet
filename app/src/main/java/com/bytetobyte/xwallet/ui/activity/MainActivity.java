package com.bytetobyte.xwallet.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.widget.TextView;
import android.widget.Toast;

import com.bytetobyte.xwallet.BlockchainClientListener;
import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.ipcmodel.BlockDownloaded;
import com.bytetobyte.xwallet.service.ipcmodel.CoinTransaction;
import com.bytetobyte.xwallet.service.ipcmodel.MnemonicSeedBackup;
import com.bytetobyte.xwallet.service.ipcmodel.SpentValueMessage;
import com.bytetobyte.xwallet.service.ipcmodel.SyncedMessage;
import com.bytetobyte.xwallet.ui.MainViewContract;
import com.bytetobyte.xwallet.ui.activity.view.MainActivityView;
import com.bytetobyte.xwallet.ui.fragment.BackupFragment;
import com.bytetobyte.xwallet.ui.fragment.TransactionFragment;
import com.bytetobyte.xwallet.ui.fragment.WalletFragment;
import com.google.gson.Gson;

import java.util.List;

/**
 *
 *
 */
public class MainActivity extends XWalletBaseActivity {

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 0x77;

    // setting boom
    public static final int INFO_CREDITS_INDEX = 0;

    public static final int BACKUP_UNLOCK_REQUEST_CODE = 0x7;

    private static final String PREFS_KEY_LAST_SYNCED_MESSAGE = "PREFS_KEY_LAST_SYNCED_MESSAGE";

    //
    private MainViewContract _mainView;

    private WalletFragment _btcWalletFragment;
    private TransactionFragment _btcTransactionsFragment;

    private WalletFragment _moneroWalletFragment;
    private TransactionFragment _moneroTransactionFragment;

    private SyncedMessage _lastSyncedMessage;
    private int _selectedMenuIndex;
    private boolean _doubleBackToExitPressedOnce;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check permissions
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (PackageManager.PERMISSION_GRANTED != permissionCheck) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    MY_PERMISSIONS_REQUEST_CAMERA);
        }

        _mainView = new MainActivityView(this);
        _mainView.initViews();

        //new TwitterAuthApi(getString(R.string.twitter_api_key), getString(R.string.twitter_api_secret), this).execute();

        if (savedInstanceState == null) {
            _btcWalletFragment = new WalletFragment();
            _btcWalletFragment.setCoin(CoinManagerFactory.BITCOIN);

            _btcTransactionsFragment = new TransactionFragment();
            _btcTransactionsFragment.setCoin(CoinManagerFactory.BITCOIN);

            _moneroWalletFragment = new WalletFragment();
            _moneroWalletFragment.setCoin(CoinManagerFactory.MONERO);

            _moneroTransactionFragment = new TransactionFragment();
            _moneroTransactionFragment.setCoin(CoinManagerFactory.MONERO);

            replaceContent(_btcWalletFragment, R.id.xwallet_main_content_layout);
            replaceContent(_btcTransactionsFragment, R.id.xwallet_secondary_content_layout);
        }
    }

    /**
     *
     */
    @Override
    protected void onResume() {
        super.onResume();

        if (getPreferencesLastSyncedMsg() == null) {
            _mainView.startTutorial();
        } else {
            _lastSyncedMessage = getPreferencesLastSyncedMsg();
//            BlockchainClientListener chainListener = (BlockchainClientListener) getSupportFragmentManager().findFragmentById(R.id.xwallet_main_content_layout);
//            if (chainListener != null) {
//                chainListener.onSyncReady(_lastSyncedMessage);
//            }
        }
    }

//    /**
//     *
//     */
//    @Override
//    protected void onStop() {
//        super.onStop();
//
//        Message msgClose = Message.obtain(null, BlockchainService.IPC_MSG_WALLET_CLOSE,  CoinManagerFactory.BITCOIN, -1);
//        sendMessage(msgClose);
//    }

    /**
     *
     */
    @Override
    protected void onServiceReady(int coinId) {
        BlockchainClientListener chainListener = (BlockchainClientListener) getSupportFragmentManager().findFragmentById(R.id.xwallet_main_content_layout);
        if (chainListener != null) {
            chainListener.onServiceReady(coinId);
        }

        super.onServiceReady(coinId);
    }

    /**
     *
     */
    @Override
    protected void onSyncReady(SyncedMessage syncedMessage) {
        System.out.println("MainActivity::onSyncReady()");
        _mainView.setSyncProgress(syncedMessage.getCoinId(), 100);

        _lastSyncedMessage = syncedMessage;

        SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        prefs.edit().putString(PREFS_KEY_LAST_SYNCED_MESSAGE, new Gson().toJson(_lastSyncedMessage)).apply();

        _btcWalletFragment.onSyncReady(syncedMessage);
        _btcTransactionsFragment.onSyncReady(syncedMessage);

        _moneroWalletFragment.onSyncReady(syncedMessage);
        _moneroTransactionFragment.onSyncReady(syncedMessage);

//        BlockchainClientListener frag = (BlockchainClientListener) getSupportFragmentManager().findFragmentById(R.id.xwallet_main_content_layout);
//        if (frag != null) {
//            frag.onSyncReady(syncedMessage);
//        }
//
//        BlockchainClientListener fragSecond = (BlockchainClientListener) getSupportFragmentManager().findFragmentById(R.id.xwallet_secondary_content_layout);
//        if (fragSecond != null) {
//            fragSecond.onSyncReady(syncedMessage);
//        }
    }

    /**
     *
     * @param block
     */
    @Override
    protected void onBlockDownloaded(BlockDownloaded block) {
        _mainView.setSyncProgress(block.getCoin(), (int) block.getPct());

        TextView textStatus = (TextView) findViewById(R.id.main_status_textview);
        textStatus.setText("Last block : " + block.getLastBlockDate());
    }

    /**
     *
     * @param feeSpentcal
     */
    @Override
    protected void onFeeCalculated(SpentValueMessage feeSpentcal) {
        BlockchainClientListener frag = (BlockchainClientListener) getSupportFragmentManager().findFragmentById(R.id.xwallet_main_content_layout);
        if (frag != null) {
            frag.onFeeCalculated(feeSpentcal);
        }
    }

    /**
     *
     * @param txs
     */
    @Override
    protected void onTransactions(List<CoinTransaction> txs) {
        BlockchainClientListener frag = (BlockchainClientListener) getSupportFragmentManager().findFragmentById(R.id.xwallet_main_content_layout);
        frag.onTransactions(txs);

        BlockchainClientListener fragSecond = (BlockchainClientListener) getSupportFragmentManager().findFragmentById(R.id.xwallet_secondary_content_layout);
        if (fragSecond != null) {
            fragSecond.onTransactions(txs);
        }
    }

    /**
     *
     * @param seedBackup
     */
    @Override
    protected void onMnemonicSeedBackup(MnemonicSeedBackup seedBackup) {
        BlockchainClientListener frag = (BlockchainClientListener) getSupportFragmentManager().findFragmentById(R.id.xwallet_main_content_layout);
        frag.onMnemonicSeedBackup(seedBackup);
    }

    /**
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    /**
     *
     * @param menuIndex - 0 = bitcoin, 1 = monero
     */
    public void showMenuSelection(int menuIndex) {
        _selectedMenuIndex = menuIndex;

        WalletFragment walletFragToShow = null;
        TransactionFragment txFragToShow = null;

        if (_selectedMenuIndex == 0) {
            walletFragToShow = _btcWalletFragment;
            txFragToShow = _btcTransactionsFragment;
        } else if (_selectedMenuIndex == 1) {
            walletFragToShow = _moneroWalletFragment;
            txFragToShow = _moneroTransactionFragment;
        }

        replaceContent(walletFragToShow, R.id.xwallet_main_content_layout);
        replaceContent(txFragToShow, R.id.xwallet_secondary_content_layout);
    }

    /**
     *
     * @param frag
     */
    public void replaceContent(Fragment frag, int contentLayoutId) {
        //remove the second container (i.e txs) when changing the main content
        FragmentManager supportManager = getSupportFragmentManager();

        if (contentLayoutId == R.id.xwallet_main_content_layout) {
            Fragment found = supportManager.findFragmentById(R.id.xwallet_secondary_content_layout);
            if (found != null) {
                supportManager
                        .beginTransaction()
                        .remove(found)
                        .commitNow();
            }
        }

        FragmentTransaction ft = supportManager.beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        ft.replace(contentLayoutId, frag);
        ft.commitNow();
    }

    /**
     *
     * @param requestCode
     * @param resultCode
     */
    @Override
    protected void onLockPinResult(int requestCode, int resultCode) {
        super.onLockPinResult(requestCode, resultCode);

        switch (resultCode) {
            case RESULT_OK:
                if (requestCode == BACKUP_UNLOCK_REQUEST_CODE) {
                    BackupFragment frag = new BackupFragment();
                    frag.setCoinId(getSelectedCoin());

                    replaceContent(frag, R.id.xwallet_main_content_layout);
                }
                break;
        }
    }

    /**
     *
     * @return
     */
    public int getSelectedCoin() {
        int coinId = CoinManagerFactory.BITCOIN;
        if (_selectedMenuIndex == 1) {
            coinId = CoinManagerFactory.MONERO;
        }

        return coinId;
    }

    /**
     *
     * @return
     */
    public SyncedMessage getLastSyncedMessage() {
        return _lastSyncedMessage;
    }

    /**
     *
     * @return
     */
    private SyncedMessage getPreferencesLastSyncedMsg() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String lastSyncMsgStr = prefs.getString(PREFS_KEY_LAST_SYNCED_MESSAGE, null);

        SyncedMessage lastSyncMsg = null;

        if (lastSyncMsgStr != null) {
            lastSyncMsg = new Gson().fromJson(lastSyncMsgStr, SyncedMessage.class);
        }

        return lastSyncMsg;
    }

    /**
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    FragmentManager supportManager = getSupportFragmentManager();
//
//                    SendFragment sendFrag = (SendFragment) supportManager.findFragmentById(R.id.xwallet_main_content_layout);
//                    sendFrag.startCameraCapture();
                } else {
                    //denied
                }
                return;
            }
        }
    }

    /**
     *
     */
    @Override
    public void onBackPressed() {
        if (_doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this._doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                _doubleBackToExitPressedOnce =false;
            }
        }, 2000);
    }



    /**
     *
     */
    public void resetCoinProgress () {
        _mainView.setSyncProgress(getSelectedCoin(), 1);
    }
}
