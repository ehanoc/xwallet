package com.bytetobyte.xwallet.service.coin.bitcoin.actions;

import android.annotation.SuppressLint;

import com.bytetobyte.xwallet.service.Constants;
import com.bytetobyte.xwallet.service.coin.CoinAction;
import com.bytetobyte.xwallet.service.coin.CurrencyCoin;
import com.bytetobyte.xwallet.service.coin.bitcoin.Bitcoin;
import com.bytetobyte.xwallet.service.coin.bitcoin.WalletUtils;
import com.bytetobyte.xwallet.service.utils.ServiceConsts;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
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

/**
 * Created by bruno on 22.03.17.
 */
public class BitcoinSetupAction extends DownloadProgressTracker implements CoinAction<CoinAction.CoinActionCallback<CurrencyCoin>> {

    private final Bitcoin _bitcoin;
    private final String _mnemonicSeed;

    private WalletAppKit _walletKit;
    private CoinActionCallback<CurrencyCoin>[] _callbacks;
    private org.bitcoinj.core.Context _bitcoinJContext;

    /**
     *
     * @param currencyCoin
     */
    public BitcoinSetupAction(CurrencyCoin<Wallet> currencyCoin) {
        this._bitcoin = (Bitcoin) currencyCoin;
        this._mnemonicSeed = null;
    }

    /**
     *
     * @param currencyCoin
     * @param mnemonicSeed
     */
    public BitcoinSetupAction(CurrencyCoin<Wallet> currencyCoin, String mnemonicSeed) {
        this._bitcoin = (Bitcoin) currencyCoin;
        this._mnemonicSeed = mnemonicSeed;
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
                if (_mnemonicSeed != null) {
                    _walletKit.peerGroup().setFastCatchupTimeSecs(1413414000);
                } else {
                    _walletKit.peerGroup().setFastCatchupTimeSecs(wallet().getEarliestKeyCreationTime());
                }

                // This is called in a background thread after startAndWait is called, as setting up various objects
                // can do disk and network IO that may cause UI jank/stuttering in wallet apps if it were to be done
                // on the main thread.
                if (wallet().getKeyChainGroupSize() < 1)
                    wallet().importKey(new ECKey());

                _walletKit.peerGroup().setBloomFilterFalsePositiveRate(0.0001);


                Wallet wallet = _walletKit.wallet();

                wallet.addCoinsReceivedEventListener(new WalletCoinsReceivedEventListener() {
                    @Override
                    public void onCoinsReceived(Wallet wallet, Transaction tx, Coin coin, Coin coin1) {
                        final Address address = WalletUtils.getWalletAddressOfReceived(tx, wallet);
                        final Coin amount = tx.getValue(wallet);
                        final TransactionConfidence.ConfidenceType confidenceType = tx.getConfidence().getConfidenceType();

                        String addressStr = WalletUtils.formatAddress(address, Constants.ADDRESS_FORMAT_GROUP_SIZE, Constants.ADDRESS_FORMAT_LINE_SIZE).toString();
                        long value = amount.getValue();

                        for (CoinActionCallback<CurrencyCoin> callback : _callbacks) {
                            callback.onCoinsReceived(addressStr, value, _bitcoin);
                        }
                    }
                });

                _bitcoin.setWallet(wallet);
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

        Date lFromDate1 = null;
        try {
            lFromDate1 = datetimeFormatter1.parse(Constants.EARLIEST_HD_WALLET_DATE);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        System.out.println("earlist date  :" + lFromDate1.getTime() / 1000);

        DeterministicSeed deterministicSeed = new DeterministicSeed(MnemonicCode.toSeed(wordList, ""), wordList, lFromDate1.getTime() / 1000);
        _walletKit.restoreWalletFromSeed(deterministicSeed);
    }
}