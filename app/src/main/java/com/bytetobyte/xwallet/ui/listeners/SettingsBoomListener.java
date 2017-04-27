package com.bytetobyte.xwallet.ui.listeners;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;

import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.ui.activity.MainActivity;
import com.bytetobyte.xwallet.ui.fragment.BackupFragment;
import com.bytetobyte.xwallet.ui.fragment.RecoverFragment;
import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.OnBoomListener;

/**
 * Created by bruno on 26.04.17.
 */
public class SettingsBoomListener implements OnBoomListener {
    //
    private final MainActivity _mainAct;

    /**
     *
     * @param activity
     */
    public SettingsBoomListener(MainActivity activity) {
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
            case MainActivity.BACKUP_BOOM_INDEX:
                newContent = new BackupFragment();
                break;

            case MainActivity.RECOVER_BOOM_INDEX:
                newContent = new RecoverFragment();
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
