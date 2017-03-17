package com.bytetobyte.xwallet;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.widget.ImageView;
import android.widget.TextView;

import com.bytetobyte.xwallet.network.api.TwitterAuthApi;
import com.bytetobyte.xwallet.service.BlockchainService;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.ipc.SpentValueMessage;
import com.bytetobyte.xwallet.view.CircleLayout;
import com.bytetobyte.xwallet.view.WheelMenuLayout;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import su.levenetc.android.badgeview.BadgeView;

/**
 *
 *
 */
public class MainActivity extends XWalletBaseActivity {

    // ACTIONS
    public static final String SEND_ACTION = "android.intent.action.SEND_COIN";

    private static final String APP_NAME = "xwallet";
    /**
     *
     */
    private BadgeView _badgeView;
    private LineChart _priceChart;

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

        initGraph();
    }

    /**
     *
     */
    private void initGraph() {
        _priceChart = (LineChart) findViewById(R.id.line_price_chart);

        List<Entry> entries = new ArrayList<Entry>();
        entries.add(new Entry(1.0f, 2.0f));
        entries.add(new Entry(2.0f, 3.0f));
        entries.add(new Entry(2.0f, 4.0f));
        entries.add(new Entry(3.0f, 2.0f));
        entries.add(new Entry(5.0f, 6.0f));

        LineDataSet dataSet = new LineDataSet(entries, "Label");

        dataSet.setDrawValues(false);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(true);

        dataSet.setDrawCircles(false);

//        dataSet.setColor(...);
//        dataSet.setValueTextColor(...); // styling, ...

        LineData lineData = new LineData(dataSet);

        _priceChart.setAutoScaleMinMaxEnabled(true);

        _priceChart.setDrawGridBackground(false);
        _priceChart.setDrawingCacheEnabled(false);
        _priceChart.setDrawMarkers(false);
        _priceChart.setDrawBorders(false);
        _priceChart.setDrawMarkers(false);

        _priceChart.getAxisLeft().setEnabled(false);
        _priceChart.getAxisRight().setEnabled(false);

        _priceChart.getXAxis().setDrawAxisLine(false);
        _priceChart.getXAxis().setDrawGridLines(false);

        _priceChart.getXAxis().setEnabled(false);

        _priceChart.getLegend().setEnabled(false);
        _priceChart.setTouchEnabled(false);

        _priceChart.setDescription(null);

        _priceChart.setData(lineData);
    }

    /**
     *
     */
    @Override
    protected void onSyncReady() {
        System.out.println("MainActivity::onSyncReady()");

        TextView balanceView = (TextView) findViewById(R.id.xwallet_balance_text);
        balanceView.setText(_syncedMessage.getAmount());
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
