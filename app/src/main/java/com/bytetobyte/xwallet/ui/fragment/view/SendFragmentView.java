package com.bytetobyte.xwallet.ui.fragment.view;

import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.ui.SendFragmentViewContract;
import com.bytetobyte.xwallet.ui.fragment.SendFragment;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by bruno on 24.04.17.
 */
public class SendFragmentView implements SendFragmentViewContract {

    private final SendFragment _sendFragment;

    private TextView _maxTextView;
    private EditText _addressEdit;
    private EditText _amountEdit;
    private CircleImageView _sendBtn;
    private SurfaceView _surfaceView;

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

        _maxTextView.setOnClickListener(_sendFragment);
        _sendBtn.setOnClickListener(_sendFragment);
        _addressEdit.addTextChangedListener(_sendFragment);
        _amountEdit.addTextChangedListener(_sendFragment);

        System.out.println("listeners setup");
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
}
