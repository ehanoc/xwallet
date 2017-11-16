package com.bytetobyte.xwallet.ui.wallet.view;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.ui.wallet.WalletFragmentViewContract;
import com.bytetobyte.xwallet.ui.wallet.WalletFragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;

/**
 * Created by bruno on 08.05.17.
 */
public class WalletFragmentView implements WalletFragmentViewContract, View.OnClickListener {

    private final WalletFragment _walletFrag;

    private ImageView _addressCopyIc;
    private TextView _balanceTxt;
    private TextView _subBalanceTxt;
    private TextView _addressTxt;
    private LineChart _priceChart;
    private TextView _coinLabel;

    /**
     *
     * @param fragment
     */
    public WalletFragmentView(WalletFragment fragment) {
        this._walletFrag = fragment;
    }

    /**
     *
     */
    @Override
    public void initViews() {
        View fragView = _walletFrag.getView();
        if (fragView == null) return;

        _addressCopyIc = (ImageView) fragView.findViewById(R.id.wallet_address_copy_ic);
        _addressCopyIc.setOnClickListener(this);

        _balanceTxt = (TextView) fragView.findViewById(R.id.wallet_balance);
        _subBalanceTxt = (TextView) fragView.findViewById(R.id.wallet_balance_sub_currency);
        _addressTxt = (TextView) fragView.findViewById(R.id.wallet_address);
        _priceChart = (LineChart) fragView.findViewById(R.id.wallet_coin_chart);
        _coinLabel = (TextView) fragView.findViewById(R.id.wallet_coin_label);

        _priceChart.setVisibility(View.INVISIBLE);
    }

    /**
     *
     * @param coinId
     */
    public void setCoinLabel(int coinId) {
        if (coinId == CoinManagerFactory.BITCOIN) {
            _coinLabel.setText("BTC");
            _coinLabel.setTextColor(_walletFrag.getBaseActivity().getResources().getColor(R.color.colorPrimary,
                    _walletFrag.getBaseActivity().getTheme()));
        } else if (coinId == CoinManagerFactory.MONERO) {
            _coinLabel.setText("XMR");
            _coinLabel.setTextColor(Color.RED);
        }
    }

    /**
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.wallet_address_copy_ic:
                _walletFrag.toClipboard(_addressTxt.getText());
                break;

            default:
                break;
        }
    }

    /**
     *
     * @param balance
     */
    @Override
    public void updateBalance(String balance) {
        _balanceTxt.setText(balance);
    }

    /**
     *
     * @param conversion
     */
    @Override
    public void updateConversion(String conversion) {
        _subBalanceTxt.setText(conversion);
    }

    /**
     *
     * @param addr
     */
    @Override
    public void updateAddress(String addr) {
        _addressTxt.setText(addr);
    }

    /**
     *
     * @param lineData
     */
    @Override
    public void updateChartPriceData(LineData lineData, int coinId) {
        _priceChart.setVisibility(View.VISIBLE);

        _priceChart.setAutoScaleMinMaxEnabled(true);

        _priceChart.setDrawGridBackground(false);
        _priceChart.setDrawingCacheEnabled(false);
        _priceChart.setDrawMarkers(false);
        _priceChart.setDrawBorders(false);

        _priceChart.getAxisLeft().setEnabled(false);
        _priceChart.getAxisRight().setEnabled(true);

        _priceChart.getAxisRight().setGranularityEnabled(true);

        float granularity = 100.0f;
        if (coinId != CoinManagerFactory.BITCOIN)
            granularity = 1.0f;

        _priceChart.getAxisRight().setGranularity(granularity);
        _priceChart.getAxisRight().setTextColor(_walletFrag.getResources().getColor(R.color.colorAccent));

        _priceChart.getXAxis().setDrawAxisLine(true);
        _priceChart.getXAxis().setDrawGridLines(false);

        _priceChart.getXAxis().setEnabled(false);

        _priceChart.getLegend().setEnabled(false);
        _priceChart.setTouchEnabled(false);

        _priceChart.setDescription(null);

        _priceChart.setData(lineData);
        _priceChart.setVisibility(View.VISIBLE);
    }
}
