package com.bytetobyte.xwallet.ui.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.ui.activity.MainActivity;
import com.bytetobyte.xwallet.ui.activity.XWalletBaseActivity;
import com.bytetobyte.xwallet.service.ipcmodel.CoinTransaction;

import java.util.List;

/**
 * Created by bruno on 19.04.17.
 */
public class TxsAdapter extends RecyclerView.Adapter<TxsAdapter.ViewHolder> {

    private final MainActivity _activity;
    private final List<CoinTransaction> _txs;

    /**
     *
     * @param baseActivity
     * @param txs
     */
    public TxsAdapter(MainActivity baseActivity, List<CoinTransaction> txs) {
        this._activity = baseActivity;
        this._txs = txs;
    }

    /**
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tx_row_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(view);
    }

    /**
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        CoinTransaction tx = _txs.get(position);

        String amount = tx.getTxAmount();

        if (getItemViewType(position) == 0) {
            holder.setBackground(R.drawable.bar_green);
            holder._sentReceivedText.setText("RECEIVED");
            holder._sentReceivedText.setTextColor(_activity.getResources().getColor(R.color.colorSecondary));
            holder._statusText.setTextColor(_activity.getResources().getColor(R.color.colorSecondary));
        }

        holder._amountText.setText(amount);
        holder._dateText.setText(tx.getTxUpdate().toString());
        holder._statusText.setText(String.format("%s CONFIRMATIONS", tx.getConfirmations()));
        holder.setCoinLabel(_activity.getSelectedCoin());
    }

    /**
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return _txs.size();
    }

    /**
     *
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        CoinTransaction tx = _txs.get(position);

        String amount = tx.getTxAmount();

        int type = 0;
        if (amount.trim().startsWith("-"))
            type = 1;

        return type;
    }

    /**
     *
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView _sentReceivedText;
        public TextView _statusText;
        public TextView _amountText;
        public TextView _dateText;
        public TextView _coinLabel;

        public ViewHolder(View v) {
            super(v);

            _sentReceivedText = (TextView) v.findViewById(R.id.txs_row_sent_received_label);
            _statusText = (TextView) v.findViewById(R.id.txs_row_status_textview);
            _amountText = (TextView) v.findViewById(R.id.txs_row_amount_text);
            _dateText = (TextView) v.findViewById(R.id.txs_row_date_textview);
            _coinLabel = (TextView) v.findViewById(R.id.txs_row_coin_label);
        }

        /**
         *
         * @param drawId
         */
        public void setBackground(int drawId) {
            itemView.setBackgroundResource(drawId);
        }

        /**
         *
         * @param coin
         */
        public void setCoinLabel(int coin) {
            if (coin == CoinManagerFactory.MONERO) {
               // _coinLabel.setTextColor(Color.RED);
                _coinLabel.setText("XMR");
            }

            if (coin == CoinManagerFactory.BITCOIN) {
                _coinLabel.setText("BTC");
            }
        }
    }
}
