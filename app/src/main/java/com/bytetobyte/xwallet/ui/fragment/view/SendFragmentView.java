package com.bytetobyte.xwallet.ui.fragment.view;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.ui.SendFragmentViewContract;
import com.bytetobyte.xwallet.ui.fragment.SendFragment;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by bruno on 24.04.17.
 */
public class SendFragmentView implements SendFragmentViewContract, View.OnClickListener, TextWatcher {

    private final SendFragment _sendFragment;

    private TextView _maxTextView;
    private EditText _addressEdit;
    private EditText _amountEdit;
    private CircleImageView _sendBtn;
    private SurfaceView _surfaceView;
    private TextView _coinLabel;

    /**
     *
     * @param sendFragment
     */
    public SendFragmentView(SendFragment sendFragment) {
        this._sendFragment = sendFragment;
    }

    /**
     *
     */
    @Override
    public void initViews() {
        View fragView = _sendFragment.getView();
        if (fragView == null) return;

        _amountEdit = (EditText) fragView.findViewById(R.id.send_amount_textview);
        _addressEdit = (EditText) fragView.findViewById(R.id.send_address_input);
        _maxTextView = (TextView) fragView.findViewById(R.id.send_max_textview);
        _sendBtn = (CircleImageView) fragView.findViewById(R.id.send_btn_id);
        _surfaceView = (SurfaceView) fragView.findViewById(R.id.send_camera_preview);
        _coinLabel = (TextView) fragView.findViewById(R.id.send_coin_label);

        if (_sendFragment.getBaseActivity().getSelectedCoin() == CoinManagerFactory.MONERO) {
            _coinLabel.setText("XMR");
            _coinLabel.setTextColor(Color.RED);
        }

        _sendBtn.setOnClickListener(this);
        _maxTextView.setOnClickListener(this);
        _addressEdit.addTextChangedListener(this);
        _amountEdit.addTextChangedListener(this);

        System.out.println("listeners setup");
    }

    /**
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        System.out.println("Clicking v : " + v);
        switch (v.getId()) {
            case R.id.send_max_textview:
                _sendFragment.onMaxAmountSelected();
                break;

            case R.id.send_btn_id:
                _sendFragment.sendAmount();
                break;

            default:
                break;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public SurfaceView getCameraSurfaceView() {
        return _surfaceView;
    }

    /**
     *
     * @return
     */
    @Override
    public EditText getAddressField() {
        return _addressEdit;
    }

    /**
     *
     * @return
     */
    @Override
    public EditText getAmountEdit() {
        return _amountEdit;
    }

    /**
     *
     * @return
     */
    @Override
    public CircleImageView getSendBtn() {
        return _sendBtn;
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
        _sendFragment.dequeueRequestTxFee();
    }

    /**
     *
     * @param s
     */
    @Override
    public void afterTextChanged(Editable s) {
        _sendFragment.queueRequestTxFee();
    }
}
