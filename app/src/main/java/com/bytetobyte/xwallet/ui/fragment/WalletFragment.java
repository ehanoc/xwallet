package com.bytetobyte.xwallet.ui.fragment;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bytetobyte.xwallet.BaseFragment;
import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.service.BlockchainService;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruno on 08.04.17.
 */
public class WalletFragment extends BaseFragment implements View.OnClickListener {

    private LineChart _priceChart;
    private TextView _balanceTxt;
    private TextView _addressTxt;
    private ImageView _addressCopyIc;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_wallet, container, false);

        //_priceChart = (LineChart) rootView.findViewById(R.id.line_price_chart);
        _addressCopyIc = (ImageView) rootView.findViewById(R.id.wallet_address_copy_ic);
        _addressCopyIc.setOnClickListener(this);

        _balanceTxt = (TextView) rootView.findViewById(R.id.wallet_balance);
        _addressTxt = (TextView) rootView.findViewById(R.id.wallet_address);
        return rootView;
    }

    /**
     *
     */
    @Override
    public void onResume() {
        super.onResume();

        //initGraph();

        Message sendMsg = Message.obtain(null, BlockchainService.IPC_MSG_WALLET_SYNC, CoinManagerFactory.BITCOIN, 0);
        getBaseActivity().sendMessage(sendMsg);
    }

    /**
     *
     */
    @Override
    public void onServiceReady() {
        getBaseActivity().syncChain(CoinManagerFactory.BITCOIN);
//        Message sendMsg = Message.obtain(null, BlockchainService.IPC_MSG_WALLET_SYNC, CoinManagerFactory.BITCOIN, 0);
//        getBaseActivity().sendMessage(sendMsg);
    }

    /**
     *
     * @param balance
     */
    public void setBalance(String balance) {
        _balanceTxt.setText(balance);
    }

    /**
     *
     * @param addresses
     */
    public void setAddress(List<String> addresses) {
        _addressTxt.setText(addresses.get(0));
    }

    /**
     *
     */
    private void initGraph() {
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
     * @param v
     */
    @Override
    public void onClick(View v) {
       switch (v.getId()) {
           case R.id.wallet_address_copy_ic:
               ClipboardManager clipboard = (ClipboardManager) getBaseActivity().getSystemService(Context.CLIPBOARD_SERVICE);
               ClipData addData = ClipData.newPlainText("btc_addr_copied", _addressTxt.getText());
               clipboard.setPrimaryClip(addData);

               Toast.makeText(getBaseActivity(), "Added to clipboard!", Toast.LENGTH_SHORT).show();
               break;

           default:
               break;
       }
    }
}
