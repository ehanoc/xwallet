package com.bytetobyte.xwallet.ui.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bytetobyte.xwallet.BaseFragment;
import com.bytetobyte.xwallet.R;
import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;
import com.bytetobyte.xwallet.service.ipcmodel.CoinTransaction;
import com.bytetobyte.xwallet.ui.adapters.TxsAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by bruno on 08.04.17.
 */
public class TransactionFragment extends BaseFragment {

    private RecyclerView _recyclerView;
    private LinearLayoutManager _layoutManager;
    private List<CoinTransaction> _txs;
    private TxsAdapter _txsAdapter;
    private TextView _emptyTxsListText;

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
        View rootView = inflater.inflate(R.layout.fragment_transactions, container, false);

        _emptyTxsListText = (TextView) rootView.findViewById(R.id.transactions_empty_list_text);
        _recyclerView = (RecyclerView) rootView.findViewById(R.id.transactions_list);

        _txs = new ArrayList<>();

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        _recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        _layoutManager = new LinearLayoutManager(getBaseActivity());
        _recyclerView.setLayoutManager(_layoutManager);

        _recyclerView.addItemDecoration(new RecyclerViewItemDecorator(10, 1));

        _txsAdapter = new TxsAdapter(getBaseActivity(), _txs);
        _recyclerView.setAdapter(_txsAdapter);

        _txsAdapter.notifyDataSetChanged();

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        getBaseActivity().requestTxList(CoinManagerFactory.BITCOIN);
    }

    /**
     *
     * @param txs
     */
    @Override
    public void onTransactions(List<CoinTransaction> txs) {
        if (txs.isEmpty()) {
            _emptyTxsListText.setVisibility(View.VISIBLE);
        }

        _txs.clear();
        _txs.addAll(txs);
        Collections.sort(_txs);
        Collections.reverse(_txs);  //descending
        _txsAdapter.notifyDataSetChanged();
    }
}
