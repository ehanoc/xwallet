package com.bytetobyte.xwallet.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bytetobyte.xwallet.BaseDialogFragment;
import com.bytetobyte.xwallet.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Created by bruno on 21.04.17.
 */
public class ReceiveFragment extends BaseDialogFragment {

    public static final String DATA_KEY_ADDR = "DATA_KEY_ADDR";
    private String _addr;
    private ImageView _qrImg;
    private TextView _addrText;

    private EditText _amountText;

    /**
     *
     */
    public ReceiveFragment() {

    }

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
        View rootView = inflater.inflate(R.layout.fragment_wallet_receive, container, false);

        _qrImg = (ImageView) rootView.findViewById(R.id.receive_id_qr_code_img);
        _addrText = (TextView) rootView.findViewById(R.id.receive_addr_text);
        _amountText = (EditText) rootView.findViewById(R.id.receive_amount_edittext);

        Bundle args = getArguments();
        if (args != null) {
            _addr = getArguments().getString(DATA_KEY_ADDR);
        }

        _amountText.addTextChangedListener(new ReceiveTextChangeListener());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        _addrText.setText(_addr);
        generateQRCode(_addr, null);
    }

    /**
     *
     * @param addr
     * @param amount
     */
    private void generateQRCode(String addr, String amount) {

        String coinName = getBaseActivity().getLastSyncedMessage().getCoinName();

        String uriStr = coinName + ":" + addr;
        if (amount != null && !amount.isEmpty()) {
            uriStr += "?amount="+amount;
        }

        try {
            QRCodeWriter writer = new QRCodeWriter();

            BitMatrix bitMatrix = writer.encode(uriStr, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            _qrImg.setImageBitmap(bmp);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     */
    private class ReceiveTextChangeListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            generateQRCode(_addr, s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}
