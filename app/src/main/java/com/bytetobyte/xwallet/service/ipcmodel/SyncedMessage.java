package com.bytetobyte.xwallet.service.ipcmodel;

import com.bytetobyte.xwallet.service.coin.CoinManagerFactory;

import java.util.List;

/**
 * Created by bruno on 29.03.17.
 */
public class SyncedMessage {
    private int _coinId;
    private String amount;
    private List<String> addresses;

    /**
     *
     * @param amount
     * @param addresses
     */
    public SyncedMessage(int coinId, String amount, List<String> addresses) {
        this._coinId = coinId;
        this.amount = amount;
        this.addresses = addresses;
    }

    public int getCoinId() {
        return _coinId;
    }

    public String getAmount() {
        return amount;
    }

    public List<String> getAddresses() {
        return addresses;
    }

    /**
     *
     * @return
     */
    public String getCoinName() {
        String name = "N/A";

        switch (_coinId) {
            case CoinManagerFactory.BITCOIN:
                name = "bitcoin";
                break;

            case CoinManagerFactory.MONERO:
                name = "monero";
                break;

            default:
                name = "U/A";
                break;
        }

        return name;
    }
}
