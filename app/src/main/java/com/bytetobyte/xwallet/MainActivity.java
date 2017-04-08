package com.bytetobyte.xwallet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bytetobyte.xwallet.fragment.WalletFragment;
import com.bytetobyte.xwallet.network.api.TwitterAuthApi;
import com.bytetobyte.xwallet.service.BlockchainService;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.ipc.SpentValueMessage;
import com.bytetobyte.xwallet.service.ipc.SyncedMessage;
import com.bytetobyte.xwallet.view.CircleLayout;
import com.bytetobyte.xwallet.view.WheelMenuLayout;
import com.google.gson.Gson;

import su.levenetc.android.badgeview.BadgeView;

/**
 *
 *
 */
public class MainActivity extends XWalletBaseActivity {

    // ACTIONS
    public static final String SEND_ACTION = "android.intent.action.SEND_COIN";

    /**
     *
     */
    private BadgeView _badgeView;

    //
    private FrameLayout _content;

    //
    private WalletFragment _walletFragment;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        new TwitterAuthApi(getString(R.string.twitter_api_key), getString(R.string.twitter_api_secret)).execute();

        if (savedInstanceState == null) {
            _walletFragment = new WalletFragment();
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

        Message sendMsg = Message.obtain(null, BlockchainService.IPC_MSG_WALLET_SYNC, CoinManagerFactory.BITCOIN, 0);
        sendMessage(sendMsg);
    }

    /**
     *
     */
    private void initViews() {
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
                        _badgeView.setValue(selectedPosition + 1);
                    }
                }
            });
        }
    }



    /**
     *
     */
    @Override
    protected void onSyncReady(SyncedMessage syncedMessage) {
        System.out.println("MainActivity::onSyncReady()");

        _walletFragment.setBalance(syncedMessage.getAmount());
        _walletFragment.setAddress(syncedMessage.getAddresses());
    }

    /**
     *
     */
    @Override
    protected void onResume() {
        super.onResume();
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

        if (getIntent().getAction() == SEND_ACTION) {
            System.out.println("MainActivity::onNewIntent! SEND_ACTION");

            SpentValueMessage spentToAmount = new SpentValueMessage("mwCwTceJvYV27KXBc3NJZys6CjsgsoeHmf", 100000000);

            Gson gson = new Gson();
            String spentToAmountJson = gson.toJson(spentToAmount);

            Message spentMsg = Message.obtain(null, BlockchainService.IPC_MSG_WALLET_SEND_AMOUNT, CoinManagerFactory.BITCOIN, 0);
            spentMsg.getData().putString(BlockchainService.IPC_BUNDLE_DATA_KEY, spentToAmountJson);
            sendMessage(spentMsg);
        }
    }
}
