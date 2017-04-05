package com.bytetobyte.xwallet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.bytetobyte.xwallet.view.WheelMenuLayout;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import su.levenetc.android.badgeview.BadgeView;

/**
 * Created by bruno on 05.04.17.
 */
public class LockScreenActivity extends AppCompatActivity {

    private Map<Integer, Integer> _pinNumbers;
    private int _nextPinNrIndex;

    private BadgeView _badgeView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_lock);

        _nextPinNrIndex = 0;

        _pinNumbers = new TreeMap<>();
        _pinNumbers.put(0, -1);
        _pinNumbers.put(1, -1);
        _pinNumbers.put(2, -1);
        _pinNumbers.put(3, -1);

        initView();
    }

    /**
     *
     */
    private void clearPin() {
        _pinNumbers.put(0, -1);
        _pinNumbers.put(1, -1);
        _pinNumbers.put(2, -1);
        _pinNumbers.put(3, -1);

        _nextPinNrIndex = 0;
    }

    /**
     *
     * @return
     */
    private boolean isPinFull() {
        for (Map.Entry<Integer, Integer> numberEntry : _pinNumbers.entrySet()) {
            if (numberEntry.getValue() == -1)
                return false;
        }

        return true;
    }

    /**
     *
     */
    private void initView() {
        WheelMenuLayout wheelMenuLayout = (WheelMenuLayout) findViewById(R.id.wheelMenu);
        if (wheelMenuLayout != null) {
            _badgeView = (BadgeView) findViewById(R.id.lense_badgeview);

            ImageView mWheelBackgroundMenu = (ImageView) findViewById(R.id.wheelmenu_background_menu);
            wheelMenuLayout.prepareWheelUIElements(null, mWheelBackgroundMenu);

            wheelMenuLayout.setWheelChangeListener(new WheelMenuLayout.WheelChangeListener() {
                @Override
                public void onSelectionChange(int selectedPosition) {
                    if (_badgeView == null) return;

                    int input = selectedPosition + 1;

                    _pinNumbers.put(_nextPinNrIndex, input);
                    _nextPinNrIndex++;

                    if (isPinFull()) {
                        clearPin();
                    }

                    displayPinCode();
                }
            });
        }
    }

    /**
     *
     */
    private void displayPinCode() {
        String pinCode = "";
        for (Map.Entry<Integer, Integer> numberEntry : _pinNumbers.entrySet()) {
            int value = numberEntry.getValue();

            if (value != -1) {
                pinCode += " * ";
            } else {
                pinCode += " - ";
            }
        }

        _badgeView.setValue(pinCode);
    }
}
