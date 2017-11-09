package com.bytetobyte.xwallet.service.coin.bitcoin;

import com.bytetobyte.xwallet.service.Constants;
import com.bytetobyte.xwallet.service.coin.CoinAction;
import com.bytetobyte.xwallet.service.coin.CoinManager;
import com.bytetobyte.xwallet.service.coin.CurrencyCoin;
import com.bytetobyte.xwallet.service.coin.bitcoin.actions.BitcoinRecoverAction;
import com.bytetobyte.xwallet.service.coin.bitcoin.actions.BitcoinSendAction;
import com.bytetobyte.xwallet.service.coin.bitcoin.actions.BitcoinSetupAction;
import com.bytetobyte.xwallet.service.ipcmodel.CoinTransaction;
import com.bytetobyte.xwallet.service.ipcmodel.SpentValueMessage;
import com.bytetobyte.xwallet.util.EncryptUtils;
import com.google.common.base.Joiner;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.crypto.KeyCrypterScrypt;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;
import org.spongycastle.crypto.params.KeyParameter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by bruno on 22.03.17.
 */
public class BitcoinManager implements CoinManager, CoinAction.CoinActionCallback, WalletCoinsReceivedEventListener {

    /**
     *
     */
    private Bitcoin _coin;
    private boolean _isSyncing;
    private boolean _isSynced;

    private static BitcoinSetupAction _setupAction;
    private static BitcoinRecoverAction _recoverAction;
    private String _walletPwd;

    /**
     *
     * @param coin
     */
    public BitcoinManager(CurrencyCoin coin) {
        this._coin = (Bitcoin) coin;
    }

    /**
     *
     */
    @Override
    public void setup(CoinAction.CoinActionCallback callback) {
        System.out.println("isSyncing :: setup ");
        _isSyncing = true;
        _isSynced = false;

        _setupAction = new BitcoinSetupAction(this);
        _setupAction.execute(callback, this);
    }

    /**
     *
     */
    @Override
    public void sendCoins(String address, String amount, CoinAction.CoinActionCallback callback) {
        BitcoinSendAction sendAction = new BitcoinSendAction(address, amount, _coin);
        sendAction.execute(callback);
    }

    /**
     *
     */
    @Override
    public void onCoinsReceived() {

    }

    /**
     *
     */
    @Override
    public void onReady() {

    }

    /**
     *
     */
    @Override
    public String getBalanceFriendlyStr() {
        //send test coins back to : mwCwTceJvYV27KXBc3NJZys6CjsgsoeHmf

        WalletAppKit walletManager = _coin.getWalletManager();
        if (walletManager == null) return null;

        Wallet wallet = walletManager.wallet();
        if (wallet == null) return null;

        Coin balance = wallet.getBalance(Wallet.BalanceType.ESTIMATED);

        String balanceStatus =
                "Friendly balance : " + balance.toFriendlyString()
                + " Estimated : " + _coin.getWalletManager().wallet().getBalance(Wallet.BalanceType.ESTIMATED)
                + " Available : " + _coin.getWalletManager().wallet().getBalance(Wallet.BalanceType.AVAILABLE)
                + " Available Spendable : " + _coin.getWalletManager().wallet().getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE)
                + " Estimated Spendable : " + _coin.getWalletManager().wallet().getBalance(Wallet.BalanceType.ESTIMATED_SPENDABLE);

        return balance.toPlainString();
    }

    /**
     *
     * @param valueMessage
     * @return
     */
    @Override
    public SpentValueMessage applyTxFee(SpentValueMessage valueMessage) {
        Coin amountCoin = Coin.parseCoin(valueMessage.getAmount());
        Address addr = Address.fromBase58(_coin.getWalletManager().wallet().getParams(), valueMessage.getAddress());
        SendRequest sendRequest = SendRequest.to(addr, amountCoin);

        Coin txValue = CalculateFeeTxSizeBytes(sendRequest.tx, sendRequest.feePerKb.getValue());
        valueMessage.setTxFee(txValue.toPlainString());

        String amountStr = amountCoin.toPlainString();
        valueMessage.setAmount(amountStr);

        return valueMessage;
    }

    /**
     *
     *  @see { https://bitcoin.stackexchange.com/questions/1195/how-to-calculate-transaction-size-before-sending }
     *
     * @param tx
     * @return
     */
    public static Coin CalculateFeeTxSizeBytes(Transaction tx, long feePerKb) {
        int nrInputs = tx.getInputs().size();
        int nrOutputs = tx.getOutputs().size();

        long txSizeKb = nrInputs * 148 + nrOutputs * 34 + 10 +- nrInputs;
        float feeV = feePerKb / (float) txSizeKb;
        Coin txValue = Coin.valueOf((long) (feeV * 10));

        return txValue;
    }

    /**
     *
     * @return
     */
    @Override
    public long getBalanceValue() {
        Coin balance = _coin.getWalletManager().wallet().getBalance(Wallet.BalanceType.ESTIMATED);
        return balance.getValue();
    }

    /**
     *
     * @return
     */
    @Override
    public Bitcoin getCurrencyCoin() {
        return _coin;
    }

    /**
     *
     * @return
     */
    @Override
    public List<String> getCurrentAddresses() {
        List<String> addrHash160List = new ArrayList<>();

        WalletAppKit walletManager = _coin.getWalletManager();
        if (walletManager != null) {
            Wallet wallet = _coin.getWalletManager().wallet();
            if (wallet != null) {
                List<Address> walletAddresses = wallet.getIssuedReceiveAddresses();
                for (Address aAddr : walletAddresses) {
                    String hash = WalletUtils.formatAddress(aAddr, Constants.ADDRESS_FORMAT_GROUP_SIZE, Constants.ADDRESS_FORMAT_LINE_SIZE).toString();
                    addrHash160List.add(hash);
                }
            }
        }

        return addrHash160List;
    }

    /**
     *
     * @return
     */
    @Override
    public Map<String, String> getAddressesKeys() {
        Map<String, String> addrKeysMap = new HashMap<>();
        List<ECKey> allWalletKeys = _coin.getWalletManager().wallet().getImportedKeys();
        allWalletKeys.addAll(_coin.getWalletManager().wallet().getActiveKeyChain().getIssuedReceiveKeys());

        for (ECKey k : allWalletKeys) {
            Address addr = k.toAddress(Constants.NETWORK_PARAMETERS);
            String hash = WalletUtils.formatAddress(addr, Constants.ADDRESS_FORMAT_GROUP_SIZE, Constants.ADDRESS_FORMAT_LINE_SIZE).toString();

            addrKeysMap.put(hash, k.getPrivateKeyAsWiF(Constants.NETWORK_PARAMETERS));
        }

        return addrKeysMap;
    }

    /**
     *
     * @return
     */
    @Override
    public String getMnemonicSeed() {
        DeterministicSeed seed = _coin.getWalletManager().wallet().getKeyChainSeed();
        String seedStr = Joiner.on(" ").join(seed.getMnemonicCode());

       // seedStr += " @" + seed.getCreationTimeSeconds();

        //System.out.println("Seed words are: " + Joiner.on(" ").join(seed.getMnemonicCode()));
        //System.out.println("Seed birthday is: " + seed.getCreationTimeSeconds());

        return seedStr;
    }

    /**
     *
     * @return
     */
    @Override
    public Date getMnemonicSeedCreationDate() {
        DeterministicSeed seed = _coin.getWalletManager().wallet().getKeyChainSeed();

        Date date = new Date();
        date.setTime(seed.getCreationTimeSeconds() * 1000);

        return date;
    }

    /**
     *
     * @param callback
     * @param seed
     */
    @Override
    public void recoverWalletBy(CoinAction.CoinActionCallback callback, String seed, Date creationDate, long blockheight) {
        System.out.println("recoverWallet with seed : " + seed);
        _isSyncing = true;
        _isSynced = false;

        _recoverAction = new BitcoinRecoverAction(this, seed, creationDate);
        _recoverAction.execute(callback, this);

        // illness bulk jewel deer chaos swing goose fetch patch blood acid call creation
        // creation time: 1490401216
//        BitcoinSetupAction setupAction = new BitcoinSetupAction(_coin, seed, creationDate);
//        setupAction.execute(callback, this);
    }

    /**
     *
     */
    @Override
    public void stopSync() {
       // _coin.getWallet().stopAsync();
    }

    /**
     *
     */
    @Override
    public void onCloseWallet() {
        System.out.println("wallet is encrypted : " + _coin.getWalletManager().wallet().isEncrypted());

        if (!_coin.getWalletManager().wallet().isEncrypted()) {
            System.out.println("wallet encrypting!");
            _coin.getWalletManager().wallet().encrypt(_walletPwd);
            System.out.println("wallet is encrypted : " + _coin.getWalletManager().wallet().isEncrypted());
        }
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isSyncing() {
        return _isSyncing;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isSynced() {
        return _isSynced;
    }

    /**
     *
     * @param result
     */
    @Override
    public void onResult(Object result) {
        System.out.println("isSyncing :: onResult");

        _isSyncing = false;
    }

    /**
     *
     * @param result
     */
    @Override
    public void onError(Object result) {
        System.out.println("isSyncing :: onError");

        _isSyncing = false;
    }

    /**
     *
     * @param coin
     */
    @Override
    public void onChainSynced(Object coin) {
        System.out.println("onChainSynced");

        _isSyncing = false;
        _isSynced = true;
    }

    /**
     *
     * @param addressStr
     * @param value
     * @param coin
     */
    @Override
    public void onCoinsReceived(String addressStr, long value, Object coin) {
        System.out.println("isSyncing :: onCoinsReceived");

        _isSyncing = false;
    }

    /**
     *  @param coin
     * @param pct
     * @param blocksLeft
     * @param date
     */
    @Override
    public void onBlocksDownloaded(CurrencyCoin coin, double pct, long blocksLeft, Date date) {

    }

    /**
     *
     */
    @Override
    public List<CoinTransaction> getTransactionList() {
        List<CoinTransaction> transactions = new ArrayList<>();

        WalletAppKit walletManager = _coin.getWalletManager();
        if (walletManager != null) {
            Set<Transaction> txs = walletManager.wallet().getTransactions(true);
            for (Transaction tx : txs) {
                Coin amount = tx.getValue(_coin.getWalletManager().wallet());

                String hash = tx.getHash().toString();
                String amountStr = amount.toPlainString();
                String fee = "";
                String confirmationStr = "0";

                if (tx.getFee() != null) {
                    fee = tx.getFee().toPlainString();
                }

                TransactionConfidence confidence = tx.getConfidence();
                try {
                    confirmationStr = Integer.toString(confidence.getDepthInBlocks());
                } catch (Exception e) { e.printStackTrace(); }
//                if (confidence.getDepthInBlocks() < 6) {
//                    confirmationStr = confidence.getDepthInBlocks() + " CONFIRMATIONS";
//                }

                TransactionConfidence.ConfidenceType cType = confidence.getConfidenceType();

                CoinTransaction coinTransaction = new CoinTransaction(_coin.getCoinId(), fee, hash, amountStr, confirmationStr, tx.getUpdateTime());
                transactions.add(coinTransaction);
            }
        }

        return transactions;
    }

    /**
     *
     * @param wallet
     * @param tx
     * @param prevBalance
     * @param newBalance
     */
    @Override
    public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
        final Address address = WalletUtils.getWalletAddressOfReceived(tx, wallet);
        final Coin amount = tx.getValue(wallet);

        //final TransactionConfidence.ConfidenceType confidenceType = tx.getConfidence().getConfidenceType();

//        String addressStr = WalletUtils.formatAddress(address, Constants.ADDRESS_FORMAT_GROUP_SIZE, Constants.ADDRESS_FORMAT_LINE_SIZE).toString();
//        long value = amount.getValue();

//        for (CoinAction.CoinActionCallback<CurrencyCoin> callback : _callbacks) {
//            callback.onCoinsReceived(addressStr, value, _bitcoin);
//        }

        // meaning that we are receiving amount, not sending
        if (amount.isPositive()) {
          //  wallet.freshReceiveAddress();
        }
    }

    /**
     *
     * @param walletPwd
     */
    @Override
    public void setWalletPwd(String walletPwd) {
        this._walletPwd = walletPwd;
    }

    /**
     *
     * @return
     */
    @Override
    public String getWalletPwd() {
        return _walletPwd;
    }
}
