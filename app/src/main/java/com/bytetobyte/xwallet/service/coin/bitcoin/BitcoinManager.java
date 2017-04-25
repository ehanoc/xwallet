package com.bytetobyte.xwallet.service.coin.bitcoin;

import com.bytetobyte.xwallet.service.Constants;
import com.bytetobyte.xwallet.service.coin.CoinAction;
import com.bytetobyte.xwallet.service.coin.CoinManager;
import com.bytetobyte.xwallet.service.coin.CurrencyCoin;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by bruno on 22.03.17.
 */
public class BitcoinManager implements CoinManager, CoinAction.CoinActionCallback {

    /**
     *
     */
    private Bitcoin _coin;
    private boolean _isSyncing;
    private BitcoinSetupAction _setupAction;
    private boolean _isSynced;

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

        _setupAction = new BitcoinSetupAction(_coin);
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

        Coin balance = _coin.getWallet().getBalance(Wallet.BalanceType.ESTIMATED);

        String balanceStatus =
                "Friendly balance : " + balance.toFriendlyString()
                + " Estimated : " + _coin.getWallet().getBalance(Wallet.BalanceType.ESTIMATED)
                + " Available : " + _coin.getWallet().getBalance(Wallet.BalanceType.AVAILABLE)
                + " Available Spendable : " + _coin.getWallet().getBalance(Wallet.BalanceType.AVAILABLE_SPENDABLE)
                + " Estimated Spendable : " + _coin.getWallet().getBalance(Wallet.BalanceType.ESTIMATED_SPENDABLE);

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
        Address addr = Address.fromBase58(_coin.getWallet().getParams(), valueMessage.getAddress());
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
        Coin balance = _coin.getWallet().getBalance(Wallet.BalanceType.ESTIMATED);
        return balance.getValue();
    }

    /**
     *
     * @return
     */
    @Override
    public CurrencyCoin getCurrencyCoin() {
        return _coin;
    }

    /**
     *
     * @return
     */
    @Override
    public List<String> getCurrentAddresses() {
        List<String> addrHash160List = new ArrayList<>();

        List<Address> walletAddresses = _coin.getWallet().getIssuedReceiveAddresses();
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

        List<ECKey> allWalletKeys = _coin.getWallet().getImportedKeys();
        allWalletKeys.addAll(_coin.getWallet().getIssuedReceiveKeys());

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
        DeterministicSeed seed = _coin.getWallet().getKeyChainSeed();
        String seedStr = Joiner.on(" ").join(seed.getMnemonicCode());

        seedStr += " creation time:" + seed.getCreationTimeSeconds();

        return seedStr;
//        System.out.println("Seed words are: " + Joiner.on(" ").join(seed.getMnemonicCode()));
//        System.out.println("Seed birthday is: " + seed.getCreationTimeSeconds());
    }

    /**
     *
     * @param callback
     * @param seed
     */
    @Override
    public void recoverWalletBy(CoinAction.CoinActionCallback callback, String seed) {
        System.out.println("isSyncing :: recoverWalletBy");
        _isSyncing = true;

        // illness bulk jewel deer chaos swing goose fetch patch blood acid call creation
        // creation time: 1490401216
        BitcoinSetupAction setupAction = new BitcoinSetupAction(_coin, seed);
        setupAction.execute(callback, this);
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
        System.out.println("isSyncing :: onChainSynced");

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

        Set<Transaction> txs = _coin.getWallet().getTransactions(true);
        for (Transaction tx : txs) {
            Coin amount = tx.getValue(_coin.getWallet());

            String hash = tx.getHash().toString();
            String amountStr = amount.toPlainString();
            String fee = "";

            if (tx.getFee() != null) {
                fee = tx.getFee().toPlainString();
            }

//            TransactionConfidence.ConfidenceType confidenceType = tx.getConfidence().getConfidenceType();
//            System.out.println("Confidence : " + confidenceType);

            CoinTransaction coinTransaction = new CoinTransaction(fee, hash, amountStr, tx.getUpdateTime());
            transactions.add(coinTransaction);
        }

        return transactions;
    }
}
