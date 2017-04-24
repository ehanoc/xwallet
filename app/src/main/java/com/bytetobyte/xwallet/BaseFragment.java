package com.bytetobyte.xwallet;

import android.content.Context;
import android.support.v4.app.Fragment;

import com.bytetobyte.xwallet.service.ipcmodel.BlockDownloaded;
import com.bytetobyte.xwallet.service.ipcmodel.CoinTransaction;
import com.bytetobyte.xwallet.service.ipcmodel.SpentValueMessage;
import com.bytetobyte.xwallet.service.ipcmodel.SyncedMessage;
import com.bytetobyte.xwallet.ui.activity.XWalletBaseActivity;

import java.util.List;

/**
 * Created by bruno on 12.04.17.
 */
public abstract class BaseFragment extends Fragment implements BlockchainClientListener {

    private XWalletBaseActivity _baseActivity;

    /**
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        _baseActivity = (XWalletBaseActivity) context;
    }

    /**
     *
     */
    @Override
    public void onResume() {
        super.onResume();

        // in case service has crashed
        if (!getBaseActivity().getIsBound()) {
            getBaseActivity().bind();
        }
    }

    /**
     *
     * @return
     */
    public XWalletBaseActivity getBaseActivity() {
        return _baseActivity;
    }

    @Override
    public void onTransactions(List<CoinTransaction> txs) {

    }

    /**
     *
     */
    @Override
    public void onServiceReady() {

    }

    @Override
    public void onSyncReady(SyncedMessage syncedMessage) {

    }

    @Override
    public void onBlockDownloaded(BlockDownloaded block) {

    }

    @Override
    public void onFeeCalculated(SpentValueMessage feeSpentcal) {

    }

}
