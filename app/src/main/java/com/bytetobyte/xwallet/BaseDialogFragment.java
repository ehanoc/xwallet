package com.bytetobyte.xwallet;

import android.content.Context;
import android.support.v4.app.DialogFragment;

import com.bytetobyte.xwallet.service.ipcmodel.BlockDownloaded;
import com.bytetobyte.xwallet.service.ipcmodel.CoinTransaction;
import com.bytetobyte.xwallet.service.ipcmodel.MnemonicSeedBackup;
import com.bytetobyte.xwallet.service.ipcmodel.SpentValueMessage;
import com.bytetobyte.xwallet.service.ipcmodel.SyncedMessage;
import com.bytetobyte.xwallet.ui.main.MainActivity;

import java.util.List;

/**
 * Created by bruno on 17.04.17.
 */
public class BaseDialogFragment extends DialogFragment implements BlockchainClientListener {

    private MainActivity _baseActivity;

    /**
     *
     * @param context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        _baseActivity = (MainActivity) context;
    }

    /**
     *
     * @return
     */
    public MainActivity getBaseActivity() {
        return _baseActivity;
    }

    @Override
    public void onServiceReady(int coinId) {

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

    @Override
    public void onTransactions(List<CoinTransaction> txs) {

    }

    @Override
    public void onMnemonicSeedBackup(MnemonicSeedBackup seedBackup) {

    }
}
