package com.bytetobyte.xwallet.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytetobyte.xwallet.BaseFragment;
import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.ui.settings.view.SettingsFragmentView;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bruno on 12.11.17.
 */

public class SettingsFragment extends BaseFragment {

    private static final String PREFS_IS_TESTNET = "PREFS_IS_TESTNET";
    private SettingsFragmentView _settingsFragView;

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
        View rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        _settingsFragView = new SettingsFragmentView(this);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        _settingsFragView.initViews();

        SharedPreferences prefs = getBaseActivity().getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        boolean isTestNet = prefs.getBoolean(PREFS_IS_TESTNET, false);
        _settingsFragView.setSwitchValue(isTestNet);
    }

    /**
     *
     * @param isChecked
     */
    public void onTestnetSettings(boolean isChecked) {
        SharedPreferences prefs = getBaseActivity().getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        prefs.edit().putBoolean(PREFS_IS_TESTNET, isChecked).apply();
    }
}
