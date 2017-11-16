package com.bytetobyte.xwallet.ui.recover.view;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.ui.recover.RecoverFragmentViewContract;
import com.bytetobyte.xwallet.ui.recover.RecoverFragment;

import java.util.Date;

/**
 *
 */
public class RecoverFragmentView implements RecoverFragmentViewContract, View.OnClickListener, TextWatcher {

    private final RecoverFragment _recoverFrag;
    private EditText _seedInput;
    private TextView _dateLabel;
    private TextView _dateDisplay;
    private ImageView _recoverBtn;

    private LinearLayout _dateRecoveryOptions;
    private LinearLayout _blockHeightRecoveryOptions;

    private EditText _blockHeightEditText;
    /**
     *
     * @param recoverFragment
     */
    public RecoverFragmentView(RecoverFragment recoverFragment) {
        this._recoverFrag = recoverFragment;
    }

    /**
     *
     */
    @Override
    public void initViews() {
        View v = _recoverFrag.getView();
        if (v == null) return;

        _seedInput = (EditText) v.findViewById(R.id.recover_seed_input);
        _dateLabel = (TextView) v.findViewById(R.id.recover_date_label_btn);
        _dateDisplay = (TextView) v.findViewById(R.id.recover_date_display);
        _recoverBtn = (ImageView) v.findViewById(R.id.recover_button);
        _blockHeightEditText = (EditText) v.findViewById(R.id.recovery_block_height_text);

        _dateRecoveryOptions = (LinearLayout) v.findViewById(R.id.recovery_date_options);
        _blockHeightRecoveryOptions = (LinearLayout) v.findViewById(R.id.recovery_block_height_options_layout);

        _seedInput.addTextChangedListener(this);
        _recoverBtn.setOnClickListener(this);
        _dateLabel.setOnClickListener(this);

        // default as in btc, its easier to remember just date
        _dateRecoveryOptions.setVisibility(View.VISIBLE);
        _blockHeightRecoveryOptions.setVisibility(View.INVISIBLE);

        if (_recoverFrag.getBaseActivity().getSelectedCoin() == CoinManagerFactory.MONERO) {
            //not needed
            _dateDisplay.setVisibility(View.INVISIBLE);
            _dateLabel.setVisibility(View.INVISIBLE);
            _dateRecoveryOptions.setVisibility(View.INVISIBLE);
            _blockHeightRecoveryOptions.setVisibility(View.VISIBLE);
        }
    }

    /**
     *
     * @return
     */
    public String getBlockHeight() {
        return _blockHeightEditText.getText().toString();
    }

    /**
     *
     * @param date
     */
    public void setDate(Date date) {
        _dateDisplay.setText(date.toString());
    }

    /**
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.recover_date_label_btn:
                _recoverFrag.launchDatePicker();
                break;

            case R.id.recover_button:
                String seed = _seedInput.getText().toString().trim();
                if (isValidMnemonicSeed(seed)) {
                    _recoverFrag.promptWalletRecovery(seed);
                }
                break;

            default:
                break;
        }
    }

    /**
     *
     * @param str
     * @return
     */
    public boolean isValidMnemonicSeed(String str) {
        if (str == null) return false;

        int words = str.trim().split("\\s+").length;

        if (_recoverFrag.getBaseActivity().getSelectedCoin() == CoinManagerFactory.MONERO)
            return words == 25;

        return (words == 12 || words == 13 || words == 25);
    }

    /**
     *
     * @param s
     * @param start
     * @param count
     * @param after
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    /**
     *
     * @param s
     * @param start
     * @param before
     * @param count
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (isValidMnemonicSeed(s.toString())) {
            _recoverBtn.setVisibility(View.VISIBLE);
        } else {
            _recoverBtn.setVisibility(View.INVISIBLE);
        }
    }

    /**
     *
     * @param s
     */
    @Override
    public void afterTextChanged(Editable s) {

    }
}
