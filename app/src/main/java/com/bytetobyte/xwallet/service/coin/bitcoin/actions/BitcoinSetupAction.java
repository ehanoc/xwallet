package com.bytetobyte.xwallet.service.coin.bitcoin.actions;

import com.bytetobyte.xwallet.service.Constants;
import com.bytetobyte.xwallet.service.coin.CoinAction;
import com.bytetobyte.xwallet.service.coin.CurrencyCoin;
import com.bytetobyte.xwallet.service.coin.bitcoin.Bitcoin;
import com.bytetobyte.xwallet.service.coin.bitcoin.BitcoinManager;
import com.bytetobyte.xwallet.service.coin.bitcoin.DownloadProgressListener;
import com.bytetobyte.xwallet.service.utils.ServiceConsts;
import com.bytetobyte.xwallet.util.EncryptUtils;

import org.bitcoinj.core.Block;
import org.bitcoinj.core.CheckpointManager;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.FilteredBlock;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Peer;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.net.discovery.DnsDiscovery;

import javax.annotation.Nullable;

/**
 * Created by bruno on 22.03.17.
 */
public class BitcoinSetupAction extends DownloadProgressListener implements CoinAction<CoinAction.CoinActionCallback<CurrencyCoin>> {

   // private final Bitcoin _bitcoin;
    private final BitcoinManager _bitcoinManger;
    private final Bitcoin _bitcoin;

    private WalletAppKit _walletKit;
    private CoinActionCallback<CurrencyCoin>[] _callbacks;
   // private org.bitcoinj.core.Context _bitcoinJContext;


    /**
     *
     * @param bitcoinManager
     */
    public BitcoinSetupAction(BitcoinManager bitcoinManager) {
        this._bitcoinManger = bitcoinManager;
        this._bitcoin = _bitcoinManger.getCurrencyCoin();
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

        _walletKit.setDownloadListener(this)
                .setBlockingStartup(false)
                .setCheckpoints(CheckpointManager.openStream(netParams))
                .setUserAgent(ServiceConsts.SERVICE_APP_NAME, "0.1");

        _walletKit.startAsync();
    }

    /**
     *
     */
    private void initWallet(final NetworkParameters netParams) {
        //File dataDir = getDir("consensus_folder", Context.MODE_PRIVATE);

      //  _bitcoinJContext = new org.bitcoinj.core.Context(netParams);

        // Start up a basic app using a class that automates some boilerplate. Ensure we always have at least one key.
        _walletKit = new WalletAppKit(Constants.NETWORK_PARAMETERS,
                _bitcoin.getDataDir(),
                ServiceConsts.SERVICE_APP_NAME + "-" + netParams.getPaymentProtocolId()) {

            /**
             *
             */
            @Override
            protected void onSetupCompleted() {
                System.out.println("Setting up wallet : " + wallet().toString());
                System.out.println("is wallet encrypted : " + wallet().isEncrypted());
                if (wallet().isEncrypted()) {
                    System.out.printf("wallet decrypting!");
                    wallet().decrypt(EncryptUtils.KSEED);
                }

                // This is called in a background thread after startAndWait is called, as setting up various objects
                // can do disk and network IO that may cause UI jank/stuttering in wallet apps if it were to be done
                // on the main thread.
                if (wallet().getKeyChainGroupSize() < 1)
                    wallet().importKey(new ECKey());

                peerGroup().setFastCatchupTimeSecs(wallet().getEarliestKeyCreationTime());
                peerGroup().addPeerDiscovery(new DnsDiscovery(netParams));

//                wallet.removeCoinsReceivedEventListener(_bitcoinManger);
//                wallet.addCoinsReceivedEventListener(_bitcoinManger);

                _bitcoin.setWalletManager(_walletKit);
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
}