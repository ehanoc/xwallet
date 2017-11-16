package com.bytetobyte.xwallet.ui.send.view;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.coin.monero.Monero;
import com.bytetobyte.xwallet.ui.send.SendFragment;
import com.m2049r.xmrwallet.model.PendingTransaction;
import com.m2049r.xmrwallet.model.Wallet;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by bruno on 16/11/2017.
 */

public class MoneroSendOptionsView implements ExtraOptionsViewContract, View.OnClickListener {

    private final SendFragment _sendFrag;
    private LinearLayout _optionsLayout;

    private EditText _paymentIdEditText;
    private Spinner _mixinsSpinner;
    private Spinner _prioritySpinner;
    private ImageView _generatePaymentIdView;

    /**
     *
     * @param sendFragment
     */
    public MoneroSendOptionsView(SendFragment sendFragment) {
        this._sendFrag = sendFragment;
    }

    /**
     *
     */
    @Override
    public void initViews() {
        View viewContainer = _sendFrag.getView();

        int selectedCoin = _sendFrag.getBaseActivity().getSelectedCoin();
        if (viewContainer == null || selectedCoin != CoinManagerFactory.MONERO) return;

        _optionsLayout = viewContainer.findViewById(R.id.send_monero_options);
        _optionsLayout.setVisibility(View.VISIBLE);

        _generatePaymentIdView = viewContainer.findViewById(R.id.monero_options_payment_id_generate_btn);
        _prioritySpinner = viewContainer.findViewById(R.id.monero_options_priority_spinner);
        _paymentIdEditText = viewContainer.findViewById(R.id.monero_options_payment_id_edittext);
        _mixinsSpinner = viewContainer.findViewById(R.id.monero_options_mixin_spinner);

        _mixinsSpinner.setAdapter(new ArrayAdapter<>(viewContainer.getContext(), android.R.layout.simple_spinner_item, Arrays.asList(Monero.Mixins)));
        _prioritySpinner.setAdapter(new ArrayAdapter<>(viewContainer.getContext(), android.R.layout.simple_spinner_item, PendingTransaction.Priority.values()));
        _generatePaymentIdView.setOnClickListener(this);
    }

    /**
     *
     * @return
     */
    @Override
    public Map<Integer, Object> getExtraInputOptions() {
        Map<Integer, Object> optionsMap = new LinkedHashMap<>();

        if (_sendFrag.getBaseActivity().getSelectedCoin() == CoinManagerFactory.MONERO) {
            optionsMap.put(Monero.KEY_TX_MIXINS, _mixinsSpinner.getSelectedItem());
            optionsMap.put(Monero.KEY_TX_PAYMENT_ID, _paymentIdEditText.getText().toString());
            optionsMap.put(Monero.KEY_TX_PRIORITY, _prioritySpinner.getSelectedItem());
        }

        return optionsMap;
    }

    /**
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.monero_options_payment_id_generate_btn:
                _paymentIdEditText.setText(Wallet.generatePaymentId());
                break;
        }
    }
}
