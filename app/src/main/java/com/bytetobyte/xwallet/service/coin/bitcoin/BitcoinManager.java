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
import com.google.common.base.Joiner;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.WalletCoinsReceivedEventListener;

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

        Coin balance = _coin.getWalletManager().wallet().getBalance(Wallet.BalanceType.ESTIMATED);

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

        valueMessage.setTxFee(sendRequest.feePerKb.toPlainString());

        long amountWithFee = amountCoin.getValue() - sendRequest.feePerKb.getValue();
        valueMessage.setAmount(Coin.valueOf(amountWithFee).toPlainString());

        return valueMessage;
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

        List<Address> walletAddresses = _coin.getWalletManager().wallet().getIssuedReceiveAddresses();
        for (Address aAddr : walletAddresses) {
            String hash = WalletUtils.formatAddress(aAddr, Constants.ADDRESS_FORMAT_GROUP_SIZE, Constants.ADDRESS_FORMAT_LINE_SIZE).toString();
            addrHash160List.add(hash);
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
        allWalletKeys.addAll(_coin.getWalletManager().wallet().getIssuedReceiveKeys());

        for (ECKey k : allWalletKeys) {
            Address addr = k.toAddress(Constants.NETWORK_PARAMETERS);
            String hash = WalletUtils.formatAddress(addr, Constants.ADDRESS_FORMAT_GROUP_SIZE, Constants.ADDRESS_FORMAT_LINE_SIZE).toString();

            addrKeysMap.put(hash, k.getPrivateKeyAsHex());
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
        System.out.println("Seed words are: " + Joiner.on(" ").join(seed.getMnemonicCode()));
        System.out.println("Seed birthday is: " + seed.getCreationTimeSeconds());

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
    public void recoverWalletBy(CoinAction.CoinActionCallback callback, String seed, Date creationDate) {
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
     *
     * @param coin
     * @param pct
     * @param blocksLeft
     * @param date
     */
    @Override
    public void onBlocksDownloaded(Object coin, double pct, int blocksLeft, Date date) {

    }

    /**
     *
     */
    @Override
    public List<CoinTransaction> getTransactionList() {
        List<CoinTransaction> transactions = new ArrayList<>();

        Set<Transaction> txs = _coin.getWalletManager().wallet().getTransactions(true);
        for (Transaction tx : txs) {
            Coin amount = tx.getValue(_coin.getWalletManager().wallet());

            String hash = tx.getHash().toString();
            String amountStr = amount.toPlainString();
            String fee = "";
            String confirmationStr = "CONFIRMED";

            if (tx.getFee() != null) {
                fee = tx.getFee().toPlainString();
            }

            TransactionConfidence confidence = tx.getConfidence();
            if (confidence.getDepthInBlocks() < 6) {
                confirmationStr = confidence.getDepthInBlocks() + " CONFIRMATIONS";
            }

            TransactionConfidence.ConfidenceType cType = confidence.getConfidenceType();

            CoinTransaction coinTransaction = new CoinTransaction(fee, hash, amountStr, confirmationStr, tx.getUpdateTime());
            transactions.add(coinTransaction);
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
}
