package com.bytetobyte.xwallet.service.coin.bitcoin.actions;

import android.annotation.SuppressLint;

import com.bytetobyte.xwallet.service.Constants;
import com.bytetobyte.xwallet.service.coin.CoinAction;
import com.bytetobyte.xwallet.service.coin.CurrencyCoin;
import com.bytetobyte.xwallet.service.coin.bitcoin.BitcoinManager;
import com.bytetobyte.xwallet.service.coin.bitcoin.DownloadProgressListener;
import com.bytetobyte.xwallet.service.utils.ServiceConsts;
import com.google.common.util.concurrent.Service;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.FilteredBlock;
import org.bitcoinj.core.Peer;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.DeterministicSeed;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

/**
 * Created by bruno on 29.04.17.
 */
public class BitcoinRecoverAction extends DownloadProgressListener implements CoinAction<CoinAction.CoinActionCallback<CurrencyCoin>> {

    private final String _seed;
    private final BitcoinManager _bitcoinManager;
    private Date _creationDate;
    private CoinActionCallback<CurrencyCoin>[] _callbacks;

    /**
     *
     * @param bitcoinManager
     * @param mnemonicSeed
     * @param creationDate
     */
    public BitcoinRecoverAction(BitcoinManager bitcoinManager, String mnemonicSeed, Date creationDate) {
        this._bitcoinManager = bitcoinManager;
        this._seed = mnemonicSeed;
        this._creationDate = creationDate;
    }

    /**
     *
     * @param callbacks
     */
    @Override
    public void execute(CoinActionCallback<CurrencyCoin>... callbacks) {
        _callbacks = callbacks;
       // reinitWallet();

        final DeterministicSeed seed = createDeterministicSeed();

        _bitcoinManager.getCurrencyCoin().getWalletManager().addListener(new Service.Listener() {
            @Override
            public void terminated(Service.State from) {
                super.terminated(from);
                WalletAppKit appKit = setupWallet();

                appKit.setDownloadListener(BitcoinRecoverAction.this)
                        .setBlockingStartup(false)
                        .setUserAgent(ServiceConsts.SERVICE_APP_NAME, "0.1")
                        .restoreWalletFromSeed(seed);

                _bitcoinManager.getCurrencyCoin().setWallet(appKit);
                _bitcoinManager.getCurrencyCoin().getWalletManager().startAsync();
            }
        }, Executors.newSingleThreadExecutor());

        _bitcoinManager.getCurrencyCoin().getWalletManager().stopAsync();
    }

    /**
     *
     */
    private WalletAppKit setupWallet() {

        final WalletAppKit appKit = new WalletAppKit(Constants.NETWORK_PARAMETERS,
                _bitcoinManager.getCurrencyCoin().getDataDir(),
                ServiceConsts.SERVICE_APP_NAME + "-" + Constants.NETWORK_PARAMETERS.getPaymentProtocolId()) {
            @Override
            protected void onSetupCompleted() {
                super.onSetupCompleted();
               // wallet().allowSpendingUnconfirmedTransactions();
            }
        };

        return appKit;
    }

    /**
     *
     * @return
     */
    private DeterministicSeed createDeterministicSeed() {
        String[] words = _seed.split(" ");
        List<String> wordList = new ArrayList<>();

        for (String word : words) {
            wordList.add(word);
        }

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat datetimeFormatter1 = new SimpleDateFormat("yyyy-MM-dd");

        if (_creationDate == null) {
            try {
                _creationDate = datetimeFormatter1.parse(Constants.EARLIEST_HD_WALLET_DATE);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        System.out.println("earliest date  :" + _creationDate.getTime() / 1000);

        final DeterministicSeed deterministicSeed = new DeterministicSeed(MnemonicCode.toSeed(wordList, ""), wordList, _creationDate.getTime() / 1000);
        return deterministicSeed;
    }

    /**
     *
     */
    @Override
    protected void doneDownload() {
        super.doneDownload();
        for (CoinActionCallback<CurrencyCoin> callback : _callbacks) {
            callback.onChainSynced(_bitcoinManager.getCurrencyCoin());
        }
    }

    /**
     *
     * @param peer
     * @param block
     * @param filteredBlock
     * @param blocksLeft
     */
    @Override
    public void onBlocksDownloaded(Peer peer, Block block, @Nullable FilteredBlock filteredBlock, int blocksLeft) {
        super.onBlocksDownloaded(peer, block, filteredBlock, blocksLeft);

        // to avoid overhead on notification, only 100th blocks or the last ones
        if (blocksLeft % 100 == 0 || blocksLeft < 10) {
            for (CoinActionCallback<CurrencyCoin> callback : _callbacks) {
                callback.onBlocksDownloaded(_bitcoinManager.getCurrencyCoin(), this.lastPercent, blocksLeft, this.lastBlockDate);
            }
        }
    }

    /**
     *
     */
    private class RecoverListener extends Service.Listener {
        /**
         *
         * @param from
         */
        @Override
        public void terminated(Service.State from) {
            super.terminated(from);
        }
    }
}
