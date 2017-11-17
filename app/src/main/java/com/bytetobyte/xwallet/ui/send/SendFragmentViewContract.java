package com.bytetobyte.xwallet.ui.send;

import android.view.SurfaceView;
import android.widget.EditText;

import com.bytetobyte.xwallet.service.ipcmodel.SpentValueMessage;
import com.bytetobyte.xwallet.ui.ViewsContract;

import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by bruno on 24.04.17.
 */
public interface SendFragmentViewContract extends ViewsContract {
    SurfaceView getCameraSurfaceView();
    CircleImageView getSendBtn();

    String getAddress();
    String getAmount();
    void setAddress(String address);
    void setAmount(String amount);

    /**
     *
     * @param spentMsgWithFee
     */
    void onFeeCalculated(SpentValueMessage spentMsgWithFee);
    void onCalculatingFee();

    /**
     * IF AVAILABLE FOR A PARTICULAR COIN
     *
     * @return empty map if not relevant to a particular coin
     */
    Map<Integer, Object> getExtraOptions();
}
