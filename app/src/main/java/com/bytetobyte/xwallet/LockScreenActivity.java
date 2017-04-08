package com.bytetobyte.xwallet;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bytetobyte.xwallet.util.EncryptUtils;
import com.bytetobyte.xwallet.view.WheelMenuLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

import su.levenetc.android.badgeview.BadgeView;

/**
 * Created by bruno on 05.04.17.
 */
public class LockScreenActivity extends AppCompatActivity {

    public static final String SET_PIN_ACTION = "android.intent.action.SET_PIN_ACTION";
    public static final String UNLOCK_PIN_ACTION = "android.intent.action.UNLOCK_PIN_ACTION";

    public static final String PREFS_KEY_PIN = "PREFS_KEY_PIN";

    public static final int REQUEST_CODE_LOCK = 0x61;

    private Map<Integer, Integer> _pinNumbers;
    private int _nextPinNrIndex;

    private TextView _titleText;
    private BadgeView _badgeView;

    private String _action;

    // to be confirmed on second run
    private Map<Integer, Integer> _setFirstInput;

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_lock);

        _action = getIntent().getAction();
        if (_action == null) {
            setResult(RESULT_CANCELED);
            finish();
        }

        _nextPinNrIndex = 0;

        _setFirstInput = new TreeMap<>();
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
                        handlePinInput();
                        clearPin();
                    }

                    displayPinCode();
                }
            });
        }

        _titleText = (TextView) findViewById(R.id.lock_title_text);
    }

    /**
     *
     */
    private void handlePinInput() {
        if (_action.equals(SET_PIN_ACTION)) {
            if (_setFirstInput.isEmpty()) {
                _setFirstInput.putAll(_pinNumbers);
                _titleText.setText(getString(R.string.lock_confirm_pin_title));
            } else {
                confirmPin();
            }
        } else if (_action.equals(UNLOCK_PIN_ACTION)) {
            tryUnlock();
        }
    }

    /**
     *
     */
    private void tryUnlock() {
        SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        String pinSet = prefs.getString(PREFS_KEY_PIN, null);
        if (pinSet == null) return;

        try {
            pinSet = EncryptUtils.getEncryptor(getBaseContext()).decrypt(pinSet);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        Type type = new TypeToken<TreeMap<Integer, Integer>>(){}.getType();
        Map<Integer, Integer> pinNumbers = gson.fromJson(pinSet, type);

        if (equalMaps(pinNumbers, _pinNumbers)) {
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(LockScreenActivity.this, "Wrong PIN input!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *
     * @return
     */
    private void confirmPin() {
        boolean isConfirmed = equalMaps(_setFirstInput, _pinNumbers);
        if (isConfirmed) {
            savePin();

            setResult(RESULT_OK);
            finish();
        } else {
            _setFirstInput.clear();
            _titleText.setText(getString(R.string.lock_enter_pin_title));

            Toast.makeText(LockScreenActivity.this, "Wrong PIN input!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     *
     */
    private void savePin() {
//        Set<String> pinNumbersString = new TreeSet<>();
//
//        for (Integer number : _pinNumbers.values()) {
//            pinNumbersString.add(number.toString());
//        }

        Gson gson = new Gson();
        String savedJsonPin = gson.toJson(_pinNumbers);

        try {
            savedJsonPin = EncryptUtils.getEncryptor(getBaseContext()).encrypt(savedJsonPin);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SharedPreferences prefs = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        prefs.edit().putString(PREFS_KEY_PIN, savedJsonPin).apply();
    }

    /**
     *
     * @param m1
     * @param m2
     * @return
     */
    private boolean equalMaps(Map<Integer,Integer>m1, Map<Integer,Integer>m2) {
        if (m1.size() != m2.size())
            return false;

        for (Integer key: m1.keySet())
            if (!m1.get(key).equals(m2.get(key)))
                return false;

        return true;
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
