package com.bytetobyte.xwallet.ui.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.bytetobyte.xwallet.BlockchainClientListener;
import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.network.api.TwitterAuthApi;
import com.bytetobyte.xwallet.network.api.models.TwitterAuthToken;
import com.bytetobyte.xwallet.service.ipcmodel.BlockDownloaded;
import com.bytetobyte.xwallet.service.ipcmodel.CoinTransaction;
import com.bytetobyte.xwallet.service.ipcmodel.MnemonicSeedBackup;
import com.bytetobyte.xwallet.service.ipcmodel.SpentValueMessage;
import com.bytetobyte.xwallet.service.ipcmodel.SyncedMessage;
import com.bytetobyte.xwallet.ui.MainViewContract;
import com.bytetobyte.xwallet.ui.activity.view.MainActivityView;
import com.bytetobyte.xwallet.ui.fragment.BackupFragment;
import com.bytetobyte.xwallet.ui.fragment.NewsFragment;
import com.bytetobyte.xwallet.ui.fragment.TransactionFragment;
import com.bytetobyte.xwallet.ui.fragment.WalletFragment;
import com.google.gson.Gson;

import java.util.List;

/**
 *
 *
 */
public class MainActivity extends XWalletBaseActivity implements TwitterAuthApi.AuthCallback {

    // main wheel boom
    public static final int SEND_BOOM_INDEX = 0;
    public static final int RECEIVE_BOOM_INDEX = 1;
    // setting boom
    public static final int BACKUP_BOOM_INDEX = 0;
    public static final int RECOVER_BOOM_INDEX = 1;
    public static final int INFO_CREDITS_INDEX = 2;

    public static final int BACKUP_UNLOCK_REQUEST_CODE = 0x7;

    private static final String PREFS_KEY_LAST_SYNCED_MESSAGE = "PREFS_KEY_LAST_SYNCED_MESSAGE";

    //
    private MainViewContract _mainView;

    private WalletFragment _walletFragment;
    private NewsFragment _newsFragment;
    private TransactionFragment _transactionsFragment;
    private TwitterAuthToken _twitterAuthToken;

    private SyncedMessage _lastSyncedMessage;


    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _mainView = new MainActivityView(this);
        _mainView.initViews();

        new TwitterAuthApi(getString(R.string.twitter_api_key), getString(R.string.twitter_api_secret), this).execute();

        if (savedInstanceState == null) {
            _walletFragment = new WalletFragment();
            _newsFragment = new NewsFragment();
            _transactionsFragment = new TransactionFragment();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.xwallet_content_layout, _walletFragment);
            ft.commit();
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
            BlockchainClientListener chainListener = (BlockchainClientListener) getSupportFragmentManager().findFragmentById(R.id.xwallet_content_layout);
            if (chainListener != null) {
                chainListener.onSyncReady(_lastSyncedMessage);
            }
        }
    }

    /**
     *
     */
    @Override
    protected void onServiceReady() {
        BlockchainClientListener chainListener = (BlockchainClientListener) getSupportFragmentManager().findFragmentById(R.id.xwallet_content_layout);
        if (chainListener != null) {
            chainListener.onServiceReady();
        }

        super.onServiceReady();
    }

    /**
     *
     */
    @Override
    protected void onSyncReady(SyncedMessage syncedMessage) {
        System.out.println("MainActivity::onSyncReady()");
        _mainView.setSyncProgress(100);

        _lastSyncedMessage = syncedMessage;

        SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        prefs.edit().putString(PREFS_KEY_LAST_SYNCED_MESSAGE, new Gson().toJson(_lastSyncedMessage)).apply();

        BlockchainClientListener frag = (BlockchainClientListener) getSupportFragmentManager().findFragmentById(R.id.xwallet_content_layout);
        if (frag != null) {
            frag.onSyncReady(syncedMessage);
        }
    }

    /**
     *
     * @param block
     */
    @Override
    protected void onBlockDownloaded(BlockDownloaded block) {
        _mainView.setSyncProgress((int) block.getPct());

        TextView textStatus = (TextView) findViewById(R.id.main_status_textview);
        textStatus.setText("Last block : " + block.getLastBlockDate());
    }

    /**
     *
     * @param feeSpentcal
     */
    @Override
    protected void onFeeCalculated(SpentValueMessage feeSpentcal) {
        BlockchainClientListener frag = (BlockchainClientListener) getSupportFragmentManager().findFragmentById(R.id.xwallet_content_layout);
        frag.onFeeCalculated(feeSpentcal);
    }

    /**
     *
     * @param txs
     */
    @Override
    protected void onTransactions(List<CoinTransaction> txs) {
        BlockchainClientListener frag = (BlockchainClientListener) getSupportFragmentManager().findFragmentById(R.id.xwallet_content_layout);
        frag.onTransactions(txs);
    }

    /**
     *
     * @param seedBackup
     */
    @Override
    protected void onMnemonicSeedBackup(MnemonicSeedBackup seedBackup) {
        BlockchainClientListener frag = (BlockchainClientListener) getSupportFragmentManager().findFragmentById(R.id.xwallet_content_layout);
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
     * @param menuIndex - 0 = wallet, 1 = txs, 2 = news
     */
    public void showMenuSelection(int menuIndex) {
        Fragment newContent = null;

        if (menuIndex == 0) {
            newContent =_walletFragment;
        } else if (menuIndex == 1) {
            newContent = _transactionsFragment;
        } else {
            newContent = _newsFragment;
        }

        replaceContent(newContent);
    }

    /**
     *
     * @param frag
     */
    public void replaceContent(Fragment frag) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        ft.replace(R.id.xwallet_content_layout, frag);
        ft.commit();
    }

    /**
     *
     * @param response
     */
    @Override
    public void onTwitterAuth(TwitterAuthToken response) {
        _twitterAuthToken = response;
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
                    replaceContent(new BackupFragment());
                }
                break;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public String getNewsAuthToken() {
        String token = null;

        if (_twitterAuthToken != null) {
            token = _twitterAuthToken.getAccessToken();
        }

        return token;
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
}
