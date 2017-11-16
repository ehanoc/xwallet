package com.bytetobyte.xwallet.ui.settings.view;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.ui.settings.SettingsFragmentViewContract;
import com.bytetobyte.xwallet.ui.settings.SettingsFragment;

/**
 * Created by bruno on 12.11.17.
 */

public class SettingsFragmentView implements SettingsFragmentViewContract, CompoundButton.OnCheckedChangeListener {

    /**
     *
     */
    private final SettingsFragment _settingsFrag;
    private Switch _switch;

    /**
     *
     * @param settingsFragment
     */
    public SettingsFragmentView(SettingsFragment settingsFragment) {
        this._settingsFrag = settingsFragment;
    }

    /**
     *
     * @param result
     */
    @Override
    public void setSwitchValue(boolean result) {
        _switch.setChecked(result);
    }

    /**
     *
     */
    @Override
    public void initViews() {
        View fragView = _settingsFrag.getView();
        if (fragView == null) return;

        _switch = (Switch) fragView.findViewById(R.id.settings_testnet_switch);
        _switch.setOnCheckedChangeListener(this);
    }

    /**
     *
     * @param buttonView
     * @param isChecked
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        _settingsFrag.onTestnetSettings(isChecked);
    }
}
