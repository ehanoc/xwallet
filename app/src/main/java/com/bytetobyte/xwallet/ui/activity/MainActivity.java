package com.bytetobyte.xwallet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import com.bytetobyte.xwallet.BlockchainClientListener;
import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.network.api.TwitterAuthApi;
import com.bytetobyte.xwallet.network.api.models.TwitterAuthToken;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.ipcmodel.BlockDownloaded;
import com.bytetobyte.xwallet.service.ipcmodel.CoinTransaction;
import com.bytetobyte.xwallet.service.ipcmodel.MnemonicSeedBackup;
import com.bytetobyte.xwallet.service.ipcmodel.SpentValueMessage;
import com.bytetobyte.xwallet.service.ipcmodel.SyncedMessage;
import com.bytetobyte.xwallet.ui.MainViewContract;
import com.bytetobyte.xwallet.ui.activity.view.MainActivityView;
import com.bytetobyte.xwallet.ui.fragment.NewsFragment;
import com.bytetobyte.xwallet.ui.fragment.TransactionFragment;
import com.bytetobyte.xwallet.ui.fragment.WalletFragment;

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
    protected void onServiceReady() {
        System.out.println("BlockchainService sending message!");

        BlockchainClientListener chainListener = (BlockchainClientListener) getSupportFragmentManager().findFragmentById(R.id.xwallet_content_layout);
        if (chainListener != null) {
            chainListener.onServiceReady();
        }

        super.onServiceReady();
//        Message sendMsg = Message.obtain(null, BlockchainService.IPC_MSG_WALLET_SYNC, CoinManagerFactory.BITCOIN, 0);
//        sendMessage(sendMsg);
    }

    /**
     *
     */
    @Override
    protected void onSyncReady(SyncedMessage syncedMessage) {
        System.out.println("MainActivity::onSyncReady()");
        _mainView.setSyncProgress(100);

        _lastSyncedMessage = syncedMessage;

        _walletFragment.setBalance(syncedMessage.getAmount());
        _walletFragment.setAddress(syncedMessage.getAddresses());
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

        System.out.println("MainActivity::onNewIntent!");

//        if (getIntent().getAction() == SEND_ACTION) {
//            System.out.println("MainActivity::onNewIntent! SEND_ACTION");
//
//            SpentValueMessage spentToAmount = new SpentValueMessage("mwCwTceJvYV27KXBc3NJZys6CjsgsoeHmf", 100000000);
//
//            Gson gson = new Gson();
//            String spentToAmountJson = gson.toJson(spentToAmount);
//
//            Message spentMsg = Message.obtain(null, BlockchainService.IPC_MSG_WALLET_SEND_AMOUNT, CoinManagerFactory.BITCOIN, 0);
//            spentMsg.getData().putString(BlockchainService.IPC_BUNDLE_DATA_KEY, spentToAmountJson);
//            sendMessage(spentMsg);
//        }
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

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        ft.replace(R.id.xwallet_content_layout, newContent);
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
}
