package com.bytetobyte.xwallet.ui.wallet;

import com.bytetobyte.xwallet.ui.ViewsContract;
import com.github.mikephil.charting.data.LineData;

/**
 * Created by bruno on 08.05.17.
 */
public interface WalletFragmentViewContract extends ViewsContract {
    void updateBalance(String balance);
    void updateConversion(String conversion);
    void updateAddress(String addr);
    void updateChartPriceData(LineData lineData, int coinId);
}
