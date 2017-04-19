package com.bytetobyte.xwallet.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.bytetobyte.xwallet.BaseDialogFragment;
import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.ipcmodel.SpentValueMessage;
import com.bytetobyte.xwallet.service.ipcmodel.SyncedMessage;

import java.math.BigDecimal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by bruno on 14.04.17.
 */
public class SendFragment extends BaseDialogFragment implements View.OnClickListener, TextWatcher {

    private TextView _maxTextView;
    private ClipboardManager _clipboard;
    private EditText _addressEdit;
    private EditText _amountEdit;
    private CircleImageView _sendBtn;

    // post delay changes
    private Handler _textHandler;
    private Runnable _textHandlerRunnable;
    private SpentValueMessage _feeToSpend;

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
        View rootView = inflater.inflate(R.layout.fragment_wallet_send, container, false);
        _clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);

        _amountEdit = (EditText) rootView.findViewById(R.id.send_amount_textview);
        _addressEdit = (EditText) rootView.findViewById(R.id.send_address_input);
        _maxTextView = (TextView) rootView.findViewById(R.id.send_max_textview);
        _sendBtn = (CircleImageView) rootView.findViewById(R.id.send_btn_id);

        _maxTextView.setOnClickListener(this);
        _sendBtn.setOnClickListener(this);
        //bitcoin regex ^[13][a-km-zA-HJ-NP-Z1-9]{25,34}$

        _addressEdit.addTextChangedListener(this);
        _amountEdit.addTextChangedListener(this);

        _textHandler = new Handler();
        return rootView;
    }

    /**
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkClipboard();
    }

    /**
     *
     */
    private void checkClipboard() {
        // If the clipboard doesn't contain data, disable the paste menu item.
        // If it does contain data, decide if you can handle the data.
        System.out.println("#1");
        if ((_clipboard.hasPrimaryClip())) {
            ClipData.Item item = _clipboard.getPrimaryClip().getItemAt(0);

            // Gets the clipboard as text.
            CharSequence pasteData = item.getText();

            Pattern p = Pattern.compile("^[13-m][a-km-zA-HJ-NP-Z1-9]{25,34}$");
            Matcher m = p.matcher(pasteData);

            if (m.matches()) {
                System.out.println("#4");
                String payload = m.group();
                _addressEdit.setText(payload);
            }
        }
    }

    /**
     *
     * @param text
     * @return
     */
    private boolean isBitcoinAddress(CharSequence text) {
        Pattern p = Pattern.compile("^[13-m][a-km-zA-HJ-NP-Z1-9]{25,34}$");
        Matcher m = p.matcher(text);
        return m.matches();
    }

    /**
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_max_textview:
                SyncedMessage lastSyncedMsg = getBaseActivity().getLastSyncedMessage();
                if (lastSyncedMsg != null) {
                    String amountStr = lastSyncedMsg.getAmount();
                    _amountEdit.setText(amountStr);
                }
                break;

            case R.id.send_btn_id:
                if (_feeToSpend != null
                        &&_addressEdit.getText().length() > 0
                        && _amountEdit.getText().length() > 0
                        && isBitcoinAddress(_addressEdit.getText())) {
                    String address = _addressEdit.getText().toString();
                    //String amount = _amountEdit.getText().toString();

                    confirmSend(address, _feeToSpend.getAmount(), _feeToSpend.getTxFee());
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
    private static long parseCoinAmountStr(String str) {
        long res = -1;
        try {
            long e = (new BigDecimal(str)).movePointRight(8).toBigIntegerExact().longValue();
            res = e;
        } catch (ArithmeticException var3) {
            throw new IllegalArgumentException(var3);
        }

        return res;
    }

    /**
     *
     * @param feeSpentcal
     */
    @Override
    public void onFeeCalculated(SpentValueMessage feeSpentcal) {
        super.onFeeCalculated(feeSpentcal);

        System.out.println("Tx fee : " + feeSpentcal.getTxFee());
//
//        View v = getView();
//        if (v == null) return;

//        _amountEdit.removeTextChangedListener(this);
//        _amountEdit.setText(feeSpentcal.getAmount());
//        _amountEdit.addTextChangedListener(this);

        _feeToSpend = feeSpentcal;
        _sendBtn.setEnabled(true);

//        TextView feeText = (TextView) v.findViewById(R.id.send_fee_textview);
//        feeText.setText("Fee :" + feeSpentcal.getTxFee());
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
        _textHandler.removeCallbacks(_textHandlerRunnable);
        _sendBtn.setEnabled(false);
    }

    /**
     *
     * @param s
     */
    @Override
    public void afterTextChanged(Editable s) {
        _textHandlerRunnable = new Runnable() {
            @Override
            public void run() {
                // check if its not higher than the max

                if (_addressEdit.getText().length() > 0
                        && _amountEdit.getText().length() > 0 && isBitcoinAddress(_addressEdit.getText())) {

                    String address = _addressEdit.getText().toString();
                    String amount = _amountEdit.getText().toString();

                    getBaseActivity().requestTxFee(address, amount, CoinManagerFactory.BITCOIN);
                }
            }
        };

        _textHandler.postDelayed(_textHandlerRunnable, 1000);
    }

    /**
     *
     */
    public void confirmSend(final String address, final String amount, final String txFee) {

        new SweetAlertDialog(getBaseActivity(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Confirm Send?")
                .setContentText("You want to send " + amount + " (Fee: "+txFee+") \n to : " + address)
                .setConfirmText("Yes, Send!")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        getBaseActivity().sendCoins(address, amount, CoinManagerFactory.BITCOIN);
                        sDialog.dismissWithAnimation();
                    }
                })
                .setCancelText("Cancel")
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.cancel();
                    }
                })
                .show();
    }

}
