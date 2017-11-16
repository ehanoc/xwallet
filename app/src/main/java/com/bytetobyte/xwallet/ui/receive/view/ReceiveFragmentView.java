package com.bytetobyte.xwallet.ui.receive.view;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.ui.receive.ReceiveFragmentViewContract;
import com.bytetobyte.xwallet.ui.receive.ReceiveFragment;

/**
 * Created by bruno on 25.04.17.
 */
public class ReceiveFragmentView implements ReceiveFragmentViewContract, TextWatcher {
    private final ReceiveFragment _receiveFragment;

    private ImageView _qrImg;
    private TextView _addrText;
    private ProgressBar _loadingQrSpinner;

    /**
     *
     * @param receiveFragment
     */
    public ReceiveFragmentView(ReceiveFragment receiveFragment) {
        this._receiveFragment = receiveFragment;
    }

    /**
     *
     */
    @Override
    public void initViews() {
        View fragView = _receiveFragment.getView();
        if (fragView == null) return;

        _loadingQrSpinner = fragView.findViewById(R.id.receive_qr_loading_spinner);
        _qrImg = (ImageView) fragView.findViewById(R.id.receive_id_qr_code_img);
        _addrText = (TextView) fragView.findViewById(R.id.receive_addr_text);

        _loadingQrSpinner.setVisibility(View.VISIBLE);
    }

    public ImageView getQrImg() {
        return _qrImg;
    }

    public TextView getAddrText() {
        return _addrText;
    }

    public ProgressBar getLoadingQrSpinner() {
        return _loadingQrSpinner;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        _receiveFragment.generateQRCode(s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
