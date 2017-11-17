package com.bytetobyte.xwallet.ui.send.view;

import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.ipcmodel.SpentValueMessage;
import com.bytetobyte.xwallet.ui.send.SendFragmentViewContract;
import com.bytetobyte.xwallet.ui.send.SendFragment;

import java.util.Map;

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

    private ExtraOptionsViewContract _extraOptions;

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

        _sendBtn.setEnabled(false);
        _sendBtn.setVisibility(View.INVISIBLE);
        _sendBtn.setOnClickListener(this);
        _maxTextView.setOnClickListener(this);
        _addressEdit.addTextChangedListener(this);
        _amountEdit.addTextChangedListener(this);

        _extraOptions = new MoneroSendOptionsView(_sendFragment);
        _extraOptions.initViews();

        System.out.println("listeners setup");
    }

    /**
     *
     * @param spentMsgWithFee
     */
    @Override
    public void onFeeCalculated(SpentValueMessage spentMsgWithFee) {
        if (null == spentMsgWithFee) return;

        String fee = spentMsgWithFee.getTxFee();
        if (fee == null || fee.isEmpty()) return;

        _sendBtn.setEnabled(true);
        _sendBtn.setVisibility(View.VISIBLE);
    }

    /**
     *
     */
    @Override
    public void onCalculatingFee() {
        _sendBtn.setEnabled(false);
        _sendBtn.setVisibility(View.INVISIBLE);
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
                _sendFragment.sendAmount(_addressEdit.getText().toString(), _amountEdit.getText().toString(), _extraOptions.getExtraInputOptions());
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
    public String getAddress() {
        return _addressEdit.getText().toString();
    }

    /**
     *
     * @return
     */
    @Override
    public String getAmount() {
        return _amountEdit.getText().toString();
    }


    /**
     *
     * @param address
     */
    @Override
    public void setAddress(String address) {
        this._addressEdit.setText(address);
    }

    /**
     *
     * @param amount
     */
    @Override
    public void setAmount(String amount) {
        this._amountEdit.setText(amount);
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

    /**
     *
     * @return
     */
    @Override
    public Map<Integer, Object> getExtraOptions() {
        return _extraOptions.getExtraInputOptions();
    }
}
