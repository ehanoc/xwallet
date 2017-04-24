package com.bytetobyte.xwallet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bytetobyte.xwallet.fragment.NewsFragment;
import com.bytetobyte.xwallet.fragment.TransactionFragment;
import com.bytetobyte.xwallet.fragment.WalletFragment;
import com.bytetobyte.xwallet.listeners.MainBoomListener;
import com.bytetobyte.xwallet.network.api.TwitterAuthApi;
import com.bytetobyte.xwallet.network.api.models.TwitterAuthToken;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.ipcmodel.BlockDownloaded;
import com.bytetobyte.xwallet.service.ipcmodel.CoinTransaction;
import com.bytetobyte.xwallet.service.ipcmodel.SpentValueMessage;
import com.bytetobyte.xwallet.service.ipcmodel.SyncedMessage;
import com.bytetobyte.xwallet.view.CircleLayout;
import com.bytetobyte.xwallet.view.WheelMenuLayout;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.nightonke.boommenu.BoomButtons.SimpleCircleButton;
import com.nightonke.boommenu.BoomMenuButton;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import su.levenetc.android.badgeview.BadgeView;

/**
 *
 *
 */
public class MainActivity extends XWalletBaseActivity implements TwitterAuthApi.AuthCallback {

    public static final int SEND_BOOM_ID = 0;
    public static final int RECEIVE_BOOM_ID = 1;

    /**
     * Views
     */
    private FrameLayout _content;
    private BadgeView _badgeView;
    private CircleImageView _lense;
    private ArcProgress _chainArcProgress;

    private BoomMenuButton _bmb;

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

        initViews();

        new TwitterAuthApi(getString(R.string.twitter_api_key), getString(R.string.twitter_api_secret), this).execute();

        if (savedInstanceState == null) {
            _walletFragment = new WalletFragment();
            _newsFragment = new NewsFragment();
            _transactionsFragment = new TransactionFragment();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(_content.getId(), _walletFragment);
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

        syncChain(CoinManagerFactory.BITCOIN);
//        Message sendMsg = Message.obtain(null, BlockchainService.IPC_MSG_WALLET_SYNC, CoinManagerFactory.BITCOIN, 0);
//        sendMessage(sendMsg);
    }

    /**
     *
     */
    private void initViews() {
        initMenuBoom();

        _chainArcProgress = (ArcProgress) findViewById(R.id.main_arc_progress);
        _lense = (CircleImageView) findViewById(R.id.lense_middle_image);
        _content = (FrameLayout) findViewById(R.id.xwallet_content_layout);

        WheelMenuLayout wheelMenuLayout = (WheelMenuLayout) findViewById(R.id.wheelMenu);
        _badgeView = (BadgeView) findViewById(R.id.lense_badgeview);

        CircleLayout mCircleLayout = (CircleLayout) findViewById(R.id.circle_layout_id);
        ImageView mWheelBackgroundMenu = (ImageView) findViewById(R.id.wheelmenu_background_menu);

        if (wheelMenuLayout != null) {
            wheelMenuLayout.prepareWheelUIElements(mCircleLayout, mWheelBackgroundMenu);
            wheelMenuLayout.setWheelChangeListener(new WheelMenuLayout.WheelChangeListener() {
                @Override
                public void onSelectionChange(int selectedPosition) {
                    if (_badgeView != null) {

                        int contentIndex = selectedPosition + 1;

                        showMenuSelection(contentIndex);
                        _badgeView.setValue(contentIndex);
                    }
                }
            });
        }

        _lense.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                _bmb.boom();
                return false;
            }
        });
    }

    /**
     *
     */
    private void initMenuBoom() {
        int[] boomsButtons = { R.drawable.ic_send, R.drawable.ic_receive};

        _bmb = (BoomMenuButton) findViewById(R.id.bmb);
        for (int i = 0; i < _bmb.getButtonPlaceEnum().buttonNumber(); i++) {
            _bmb.addBuilder(new SimpleCircleButton.Builder()
                    .normalImageRes(boomsButtons[i])
            );
        }

        _bmb.setOnBoomListener(new MainBoomListener(this));
    }


    /**
     *
     */
    @Override
    protected void onSyncReady(SyncedMessage syncedMessage) {
        System.out.println("MainActivity::onSyncReady()");
        _chainArcProgress.setProgress(100);

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
        _chainArcProgress.setProgress((int) block.getPct());

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
     * @param menuIndex
     */
    private void showMenuSelection(int menuIndex) {
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
