package com.bytetobyte.xwallet.service.coin.bitcoin.actions;

import android.annotation.SuppressLint;

import com.bytetobyte.xwallet.service.Constants;
import com.bytetobyte.xwallet.service.coin.CoinAction;
import com.bytetobyte.xwallet.service.coin.CurrencyCoin;
import com.bytetobyte.xwallet.service.coin.bitcoin.Bitcoin;
import com.bytetobyte.xwallet.service.coin.bitcoin.DownloadProgressListener;
import com.bytetobyte.xwallet.service.coin.bitcoin.WalletUtils;
import com.bytetobyte.xwallet.service.utils.ServiceConsts;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Block;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.FilteredBlock;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.listeners.DownloadProgressTracker;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by bruno on 22.03.17.
 */
public class BitcoinSetupAction extends DownloadProgressListener implements CoinAction<CoinAction.CoinActionCallback<CurrencyCoin>> {

    private final Bitcoin _bitcoin;
    private final String _mnemonicSeed;
    private final Date _date;

    private WalletAppKit _walletKit;
    private CoinActionCallback<CurrencyCoin>[] _callbacks;
    private org.bitcoinj.core.Context _bitcoinJContext;


    /**
     *
     * @param currencyCoin
     */
    public BitcoinSetupAction(CurrencyCoin<WalletAppKit> currencyCoin) {
        this._bitcoin = (Bitcoin) currencyCoin;
        this._mnemonicSeed = null;
        this._date = null;
    }

    /**
     *
     * @param currencyCoin
     * @param mnemonicSeed
     */
    public BitcoinSetupAction(CurrencyCoin<WalletAppKit> currencyCoin, String mnemonicSeed, Date creationDate) {
        this._bitcoin = (Bitcoin) currencyCoin;
        this._mnemonicSeed = mnemonicSeed;
        this._date = creationDate;
    }

    /**
     *
     * @param callbacks
     */
    @Override
    public void execute(CoinActionCallback<CurrencyCoin>... callbacks) {
        this._callbacks = callbacks;
        NetworkParameters netParams = Constants.NETWORK_PARAMETERS;

        initWallet(netParams);

        if (_mnemonicSeed != null) {
            recoverWalletFromSeed();
        }

        _walletKit.setDownloadListener(this)
                .setBlockingStartup(false)
                .setCheckpoints(CheckpointManager.openStream(netParams))
                .setUserAgent(ServiceConsts.SERVICE_APP_NAME, "0.1");

        _walletKit.startAsync();
    }

    /**
     *
     */
    private void initWallet(NetworkParameters netParams) {
        //File dataDir = getDir("consensus_folder", Context.MODE_PRIVATE);

        _bitcoinJContext = new org.bitcoinj.core.Context(netParams);

        // Start up a basic app using a class that automates some boilerplate. Ensure we always have at least one key.
        _walletKit = new WalletAppKit(_bitcoinJContext, _bitcoin.getDataDir(), ServiceConsts.SERVICE_APP_NAME) {

            /**
             *
             */
            @Override
            protected void onSetupCompleted() {
                // This is called in a background thread after startAndWait is called, as setting up various objects
                // can do disk and network IO that may cause UI jank/stuttering in wallet apps if it were to be done
                // on the main thread.
                if (wallet().getKeyChainGroupSize() < 1)
                    wallet().importKey(new ECKey());

                if (_mnemonicSeed != null && _date != null) {
                    _walletKit.peerGroup().setFastCatchupTimeSecs(_date.getTime());
                } else {
                    _walletKit.peerGroup().setFastCatchupTimeSecs(wallet().getEarliestKeyCreationTime());
                }

                _walletKit.peerGroup().setBloomFilterFalsePositiveRate(0.0001);


                Wallet wallet = _walletKit.wallet();

                wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
                    @Override
                    public void onCoinsReceived(Wallet wallet, Transaction tx, Coin coin, Coin coin1) {
                        final Address address = WalletUtils.getWalletAddressOfReceived(tx, wallet);
                        final Coin amount = tx.getValue(wallet);
                        //final TransactionConfidence.ConfidenceType confidenceType = tx.getConfidence().getConfidenceType();

                        String addressStr = WalletUtils.formatAddress(address, Constants.ADDRESS_FORMAT_GROUP_SIZE, Constants.ADDRESS_FORMAT_LINE_SIZE).toString();
                        long value = amount.getValue();

                        for (CoinActionCallback<CurrencyCoin> callback : _callbacks) {
                            callback.onCoinsReceived(addressStr, value, _bitcoin);
                        }

                        // meaning that we are receiving amount, not sending
                        if (amount.isPositive()) {
                            wallet.freshReceiveAddress();
                        }
                    }
                });

                _bitcoin.setWallet(_walletKit);
            }
        };
    }

    /**
     *
     */
    @Override
    protected void doneDownload() {
        super.doneDownload();
        for (CoinActionCallback<CurrencyCoin> callback : _callbacks) {
            callback.onChainSynced(_bitcoin);
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
                callback.onBlocksDownloaded(_bitcoin, this.lastPercent, blocksLeft, this.lastBlockDate);
            }
        }
    }

    /**
     *
     */
    private void recoverWalletFromSeed() {
        if (_mnemonicSeed == null) return;

        String[] words = _mnemonicSeed.split(" ");
        List<String> wordList = new ArrayList<>();

        for (String word : words) {
            wordList.add(word);
        }

        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat datetimeFormatter1 = new SimpleDateFormat("yyyy-MM-dd");

        Date lFromDate1 = _date;

        if (lFromDate1 == null) {
            try {
                lFromDate1 = datetimeFormatter1.parse(Constants.EARLIEST_HD_WALLET_DATE);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        System.out.println("earlist date  :" + lFromDate1.getTime() / 1000);

        DeterministicSeed deterministicSeed = new DeterministicSeed(MnemonicCode.toSeed(wordList, ""), wordList, lFromDate1.getTime() / 1000);
        _walletKit.restoreWalletFromSeed(deterministicSeed);
    }
}