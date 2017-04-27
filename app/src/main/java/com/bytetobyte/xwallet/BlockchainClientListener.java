package com.bytetobyte.xwallet;

import com.bytetobyte.xwallet.service.ipcmodel.BlockDownloaded;
import com.bytetobyte.xwallet.service.ipcmodel.CoinTransaction;
import com.bytetobyte.xwallet.service.ipcmodel.MnemonicSeedBackup;
import com.bytetobyte.xwallet.service.ipcmodel.SpentValueMessage;
import com.bytetobyte.xwallet.service.ipcmodel.SyncedMessage;

import java.util.List;

/**
 * Created by bruno on 18.04.17.
 */
public interface BlockchainClientListener {
      void onServiceReady();
      void onSyncReady(SyncedMessage syncedMessage);
      void onBlockDownloaded(BlockDownloaded block);
      void onFeeCalculated(SpentValueMessage feeSpentcal);
      void onTransactions(List<CoinTransaction> txs);
      void onMnemonicSeedBackup(MnemonicSeedBackup seedBackup);
}
