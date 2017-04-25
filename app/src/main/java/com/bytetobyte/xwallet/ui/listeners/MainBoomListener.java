package com.bytetobyte.xwallet.ui.listeners;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;

import com.bytetobyte.xwallet.ui.activity.MainActivity;
import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.ui.fragment.ReceiveFragment;
import com.bytetobyte.xwallet.ui.fragment.SendFragment;
import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.OnBoomListener;

import java.util.List;

/**
 * Created by bruno on 15.04.17.
 */
public class MainBoomListener implements OnBoomListener {

    //
    private final MainActivity _mainAct;

    /**
     *
     * @param activity
     */
    public MainBoomListener(MainActivity activity) {
        this._mainAct = activity;
    }

    /**
     *
     * @param index
     * @param boomButton
     */
    @Override
    public void onClicked(int index, BoomButton boomButton) {
        DialogFragment newContent = null;

        switch (index) {
            case MainActivity.SEND_BOOM_ID:
                newContent = new SendFragment();
                break;

            case MainActivity.RECEIVE_BOOM_ID:
                if (_mainAct.getLastSyncedMessage() == null) return;

                List<String> addrs = _mainAct.getLastSyncedMessage().getAddresses();
                if (addrs.size() > 0) {
                    String lastAddr = addrs.get(addrs.size() - 1);
                    newContent = new ReceiveFragment();

                    Bundle receiveData = new Bundle();
                    receiveData.putString(ReceiveFragment.DATA_KEY_ADDR, lastAddr);
                    newContent.setArguments(receiveData);
                }

                break;

            default:
                break;
        }

        FragmentTransaction ft = _mainAct.getSupportFragmentManager().beginTransaction();
        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        ft.replace(R.id.xwallet_content_layout, newContent);
        ft.commit();
    }

    /**
     *
     */
    @Override
    public void onBackgroundClick() {

    }

    /**
     *
     */
    @Override
    public void onBoomWillHide() {

    }

    /**
     *
     */
    @Override
    public void onBoomDidHide() {

    }

    /**
     *
     */
    @Override
    public void onBoomWillShow() {

    }

    /**
     *
     */
    @Override
    public void onBoomDidShow() {

    }
}
