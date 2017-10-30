package com.bytetobyte.xwallet.ui.fragment;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytetobyte.xwallet.BaseDialogFragment;
import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.ipcmodel.SpentValueMessage;
import com.bytetobyte.xwallet.service.ipcmodel.SyncedMessage;
import com.bytetobyte.xwallet.ui.SendFragmentViewContract;
import com.bytetobyte.xwallet.ui.fragment.view.SendFragmentView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.pedant.SweetAlert.SweetAlertDialog;
import github.nisrulz.qreader.QRDataListener;
import github.nisrulz.qreader.QREader;

/**
 * Created by bruno on 14.04.17.
 */
public class SendFragment extends BaseDialogFragment {

    // post delay changes
    private Handler _handler;
    private Runnable _textHandlerRunnable;
    private SpentValueMessage _feeToSpend;

    private ClipboardManager _clipboard;
    private QREader qrEader;
    private String _latestDetectedData;

    private SendFragmentViewContract _sendViewContract;

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

        _sendViewContract = new SendFragmentView(this);
        _handler = new Handler();
        return rootView;
    }

    /**
     *
     */
    @Override
    public void onResume() {
        super.onResume();
        startCameraCapture();
    }

    /**
     *
     */
    @Override
    public void onPause() {
        super.onPause();

        // Cleanup in onPause()
        // --------------------
        qrEader.releaseAndCleanup();
    }

    /**
     *
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        _sendViewContract.initViews();

        // Init QREader
        // ------------
        qrEader = new QREader.Builder(getBaseActivity(), _sendViewContract.getCameraSurfaceView(), new QRDataListener() {
            @Override
            public void onDetected(final String data) {
                if ( (data == null || data.equals(_latestDetectedData)) && _sendViewContract.getAddressField().getText().length() > 0) return;

                _latestDetectedData = data;
                getBaseActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // check if data includes address + amount + currency (i.e, bitcoin : [addr] ? amount = [amount])
                        String addr = _latestDetectedData;
                        if (_latestDetectedData.contains(":")) {
                            String[] tokens = _latestDetectedData.split(":");
                            if (tokens.length > 1) {
                                if (isBitcoinAddress(tokens[1])) {
                                    addr = tokens[1];
                                } else if (tokens[1].contains("?")) {
                                    String[] addrAmountTokens = tokens[1].split("\\?");
                                    addr = addrAmountTokens[0];
                                }
                            }
                        }

                        _sendViewContract.getAddressField().setText(addr);
                    }
                });
            }
        }).facing(QREader.BACK_CAM)
                .enableAutofocus(true)
                .height(_sendViewContract.getCameraSurfaceView().getHeight())
                .width(_sendViewContract.getCameraSurfaceView().getWidth())
                .build();

        //checkClipboard();
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
     * @param feeSpentcal
     */
    @Override
    public void onFeeCalculated(SpentValueMessage feeSpentcal) {
        super.onFeeCalculated(feeSpentcal);

        _feeToSpend = feeSpentcal;
        _sendViewContract.getSendBtn().setEnabled(true);
    }

    /**
     *
     */
    public void queueRequestTxFee() {
        _textHandlerRunnable = new Runnable() {
            @Override
            public void run() {
                // check if its not higher than the max

                if (_sendViewContract.getAddressField().getText().length() > 0
                        && _sendViewContract.getAmountEdit().getText().length() > 0 && isBitcoinAddress(_sendViewContract.getAddressField().getText())) {

                    String address = _sendViewContract.getAddressField().getText().toString();
                    String amount = _sendViewContract.getAmountEdit().getText().toString();

                    getBaseActivity().requestTxFee(address, amount, CoinManagerFactory.BITCOIN);
                }
            }
        };

        _handler.postDelayed(_textHandlerRunnable, 1000);
    }

    /**
     *
     */
    public void dequeueRequestTxFee() {
        _handler.removeCallbacks(_textHandlerRunnable);
        _sendViewContract.getSendBtn().setEnabled(false);
    }

    /**
     *
     */
    public void onMaxAmountSelected() {
        SyncedMessage lastSyncedMsg = getBaseActivity().getLastSyncedMessage();
        if (lastSyncedMsg != null) {
            String amountStr = lastSyncedMsg.getAmount();
            _sendViewContract.getAmountEdit().setText(amountStr);
        }
    }

    /**
     *
     */
    public void sendAmount() {
        if (_feeToSpend != null
                &&_sendViewContract.getAddressField().getText().length() > 0
                && _sendViewContract.getAmountEdit().getText().length() > 0
                && isBitcoinAddress(_sendViewContract.getAddressField().getText())) {
            String address = _sendViewContract.getAddressField().getText().toString();
            //String amount = _amountEdit.getText().toString();

            confirmSend(address, _feeToSpend.getAmount(), _feeToSpend.getTxFee());
        }
    }

    /**
     *
     */
    public void confirmSend(final String address, final String amount, final String txFee) {

        new SweetAlertDialog(getBaseActivity(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Confirm Send?")
                .setContentText("You want to send " + amount + " (Estimated Fee: "+txFee+") \n to : " + address)
                .setConfirmText("Yes, Send!")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        getBaseActivity().sendCoins(address, amount, CoinManagerFactory.BITCOIN);
                        sDialog.dismissWithAnimation();
                        getBaseActivity().showMenuSelection(0);
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

    /**
     *
     */
    public void startCameraCapture() {
        qrEader.initAndStart(_sendViewContract.getCameraSurfaceView());
    }

}
