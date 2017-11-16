package com.bytetobyte.xwallet.ui.receive;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bytetobyte.xwallet.BaseDialogFragment;
import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.ui.receive.view.ReceiveFragmentView;
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

    /**
     *
     */
    private ReceiveFragmentView _receiveFragmentView;

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

        _receiveFragmentView = new ReceiveFragmentView(this);

        Bundle args = getArguments();
        if (args != null) {
            _addr = getArguments().getString(DATA_KEY_ADDR);
        }

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
        _receiveFragmentView.initViews();
    }

    @Override
    public void onResume() {
        super.onResume();

        _receiveFragmentView.getAddrText().setText(_addr);
        new Thread(new Runnable() {
            @Override
            public void run() {
                generateQRCode(null);
            }
        }).start();
    }

    /**
     *
     * @param amount
     */
    public void generateQRCode(String amount) {

        String coinName = getBaseActivity().getLastSyncedMessage().getCoinName();

        String uriStr = coinName + ":" + _addr;
        if (amount != null && !amount.isEmpty()) {
            uriStr += "?amount="+amount;
        }

        try {
            QRCodeWriter writer = new QRCodeWriter();

            BitMatrix bitMatrix = writer.encode(uriStr, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            final Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }

            getBaseActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    _receiveFragmentView.getLoadingQrSpinner().setVisibility(View.GONE);
                    _receiveFragmentView.getQrImg().setImageBitmap(bmp);
                }
            });
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}
