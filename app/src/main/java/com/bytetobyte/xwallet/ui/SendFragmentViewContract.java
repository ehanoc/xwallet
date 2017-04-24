package com.bytetobyte.xwallet.ui;

import android.view.SurfaceView;
import android.widget.EditText;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by bruno on 24.04.17.
 */
public interface SendFragmentViewContract extends ViewContract {
    SurfaceView getCameraSurfaceView();
    EditText getAddressField();
    CircleImageView getSendBtn();

    EditText getAmountEdit();
}
