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
import android.widget.Toast;

import com.bytetobyte.xwallet.BaseFragment;
import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.network.api.CexChartAPI;
import com.bytetobyte.xwallet.network.api.PriceRequestAPI;
import com.bytetobyte.xwallet.network.api.models.CexCharItem;
import com.bytetobyte.xwallet.network.api.models.MinApiDataItem;
import com.bytetobyte.xwallet.network.api.models.MinApiResult;
import com.bytetobyte.xwallet.service.BlockchainService;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.ipcmodel.SyncedMessage;
import com.bytetobyte.xwallet.ui.fragment.view.WalletFragmentView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Created by bruno on 08.04.17.
 */
public class WalletFragment extends BaseFragment implements CexChartAPI.CexChartCallback, PriceRequestAPI.PriceAPICallback {

    private List<Entry> _chartEntries;

    private WalletFragmentView _walletFragView;
    private String _lastBalance;

    // bitcoin as default
    private int _coinId = CoinManagerFactory.BITCOIN;

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

        _walletFragView = new WalletFragmentView(this);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _walletFragView.initViews();
        _walletFragView.setCoinLabel(this._coinId);
    }

    /**
     *
     */
    @Override
    public void onResume() {
        super.onResume();
        getBaseActivity().syncChain(this._coinId);

        //new CexChartAPI(this).execute();
        new PriceRequestAPI(PriceRequestAPI.GetCoinUrl(_coinId), this).execute();
    }

    /**
     *
     */
    @Override
    public void onServiceReady(int coinId) {
        getBaseActivity().syncChain(this._coinId);
    }

    /**
     *
     * @param syncedMessage
     */
    @Override
    public void onSyncReady(SyncedMessage syncedMessage) {
        super.onSyncReady(syncedMessage);

        if (syncedMessage.getCoinId() != this._coinId) return;

        updateBalance(syncedMessage.getAmount());
        setAddress(syncedMessage.getAddresses());
    }

    /**
     *
     * @param balance
     */
    public void updateBalance(String balance) {
        if (balance == null) return;

       // _balanceTxt.setText(balance);
        _lastBalance = balance;
        _walletFragView.updateBalance(balance);

        if(_chartEntries != null && !_chartEntries.isEmpty()) {
            Entry latestValue = _chartEntries.get(_chartEntries.size() - 1);
            Double actualBalance = Double.parseDouble(balance);
            Locale locale = new Locale("en", "US");
            NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(locale);

            double conversionValue = actualBalance * latestValue.getY();
            String subBalance = String.format(Locale.getDefault(), "(%s$)", currencyFormatter.format(conversionValue));
            //_subBalanceTxt.setText(subBalance);
            _walletFragView.updateConversion(subBalance);
        }
    }

    /**
     *
     * @param addresses
     */
    public void setAddress(List<String> addresses) {
        if (addresses.isEmpty()) return;

        _walletFragView.updateAddress(addresses.get(addresses.size() - 1));
    }

    /**
     *
     */
    private void updateGraph(List<Entry> entries) {
        LineDataSet dataSet = new LineDataSet(entries, "Label");

        dataSet.setDrawValues(false);
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(true);
        dataSet.setDrawCircles(false);

        LineData lineData = new LineData(dataSet);
        _walletFragView.updateChartPriceData(lineData, _coinId);
    }

    /**
     *
     * @param items
     */
    @Override
    public void onChartResult(List<CexCharItem> items) {
        _chartEntries = new ArrayList<Entry>();

        for (int i = 0; i < items.size(); i ++) {
            Float price = Float.parseFloat(items.get(i).getPrice());
            Long timestamp = Long.parseLong(items.get(i).getTmsp());
            _chartEntries.add(new Entry(timestamp, price));
        }

        Collections.sort(_chartEntries, new EntryXComparator());
        updateGraph(_chartEntries);
        updateBalance(_lastBalance);
    }

    /**
     *
     * @param result
     */
    @Override
    public void onPriceResult(MinApiResult result) {
        List<MinApiDataItem> items = result.getData();

        _chartEntries = new ArrayList<Entry>();

        for (int i = 0; i < items.size(); i ++) {
            Float price = items.get(i).getClose();
            Long timestamp = items.get(i).getTime();
            _chartEntries.add(new Entry(timestamp, price));
        }

        Collections.sort(_chartEntries, new EntryXComparator());
        updateGraph(_chartEntries);
        updateBalance(_lastBalance);
    }

    /**
     *
     * @param text
     */
    public void toClipboard(CharSequence text) {
        ClipboardManager clipboard = (ClipboardManager) getBaseActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData addData = ClipData.newPlainText("btc_addr_copied", text);
        clipboard.setPrimaryClip(addData);

        Toast.makeText(getBaseActivity(), "Added to clipboard!", Toast.LENGTH_SHORT).show();
    }

    /**
     *
     * @param coinId
     */
    public void setCoin(int coinId) {
        this._coinId = coinId;
    }
}
