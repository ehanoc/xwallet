package com.bytetobyte.xwallet.ui.listeners;

import com.bytetobyte.xwallet.BaseFragment;
import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.ui.main.MainActivity;
import com.bytetobyte.xwallet.ui.info.InfoFragment;
import com.bytetobyte.xwallet.ui.settings.SettingsFragment;
import com.nightonke.boommenu.BoomButtons.BoomButton;
import com.nightonke.boommenu.OnBoomListener;

/**
 * Created by bruno on 26.04.17.
 */
public class SettingsBoomListener implements OnBoomListener {
    // setting boom
    public static final int INFO_CREDITS_INDEX = 0;
    public static final int SETTINGS_INDEX = 1;
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
        BaseFragment newContent = null;

        switch (index) {
//            case MainActivity.BACKUP_BOOM_INDEX:
//                //newContent = new BackupFragment();
//                _mainAct.toLock(MainActivity.BACKUP_UNLOCK_REQUEST_CODE);
//                break;
//
//            case MainActivity.RECOVER_BOOM_INDEX:
//                newContent = new RecoverFragment();
//                _mainAct.replaceContent(newContent, R.id.xwallet_content_layout);
//                break;

            case INFO_CREDITS_INDEX:
                newContent = new InfoFragment();
                _mainAct.replaceContent(newContent, R.id.xwallet_main_content_layout);
                break;

            case SETTINGS_INDEX:
                newContent = new SettingsFragment();
                _mainAct.replaceContent(newContent, R.id.xwallet_main_content_layout);
                break;

            default:
                break;
        }

//        FragmentTransaction ft = _mainAct.getSupportFragmentManager().beginTransaction();
//        ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
//        ft.replace(R.id.xwallet_content_layout, newContent);
//        ft.commit();
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
